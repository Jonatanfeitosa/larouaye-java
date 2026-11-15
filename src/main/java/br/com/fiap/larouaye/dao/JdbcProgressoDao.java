package br.com.fiap.larouaye.dao;

import br.com.fiap.larouaye.model.Progresso;
import br.com.fiap.larouaye.model.StatusEtapa;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** DAO do progresso (tabela associativa N:N cidadão x etapa) com JDBC puro. */
@Repository
public class JdbcProgressoDao implements ProgressoDao {

    private static final String SELECT = "SELECT cd_cidadao, cd_etapa, ds_status, dt_atualizacao FROM tb_cidadao_etapa ";

    private final DataSource dataSource;

    public JdbcProgressoDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public List<Progresso> listarPorCidadao(Long cidadaoId) {
        try (Connection con = dataSource.getConnection(); PreparedStatement ps = con.prepareStatement(SELECT + "WHERE cd_cidadao = ? ORDER BY cd_etapa")) {
            ps.setLong(1, cidadaoId);
            List<Progresso> lista = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
            return lista;
        } catch (SQLException e) {
            throw JdbcUtil.traduzir(e, "listar progresso do cidadão");
        }
    }

    /** Tenta o UPDATE primeiro; se nenhuma linha mudou, faz o INSERT. Tudo na mesma transação. */
    @Override
    public Progresso salvar(Progresso p) {
        String update = "UPDATE tb_cidadao_etapa SET ds_status = ?, dt_atualizacao = SYSTIMESTAMP WHERE cd_cidadao = ? AND cd_etapa = ?";
        String insert = "INSERT INTO tb_cidadao_etapa (cd_cidadao, cd_etapa, ds_status) VALUES (?, ?, ?)";
        try (Connection con = dataSource.getConnection()) {
            con.setAutoCommit(false);
            try {
                int linhas;
                try (PreparedStatement ps = con.prepareStatement(update)) {
                    ps.setString(1, p.getStatus().name());
                    ps.setLong(2, p.getCidadaoId());
                    ps.setLong(3, p.getEtapaId());
                    linhas = ps.executeUpdate();
                }
                if (linhas == 0) {
                    try (PreparedStatement ps = con.prepareStatement(insert)) {
                        ps.setLong(1, p.getCidadaoId());
                        ps.setLong(2, p.getEtapaId());
                        ps.setString(3, p.getStatus().name());
                        ps.executeUpdate();
                    }
                }
                Progresso gravado;
                try (PreparedStatement ps = con.prepareStatement(SELECT + "WHERE cd_cidadao = ? AND cd_etapa = ?")) {
                    ps.setLong(1, p.getCidadaoId());
                    ps.setLong(2, p.getEtapaId());
                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        gravado = mapear(rs);
                    }
                }
                con.commit();
                return gravado;
            } catch (SQLException | RuntimeException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw JdbcUtil.traduzir(e, "salvar progresso");
        }
    }

    private static Progresso mapear(ResultSet rs) throws SQLException {
        Progresso p = new Progresso();
        p.setCidadaoId(rs.getLong("cd_cidadao"));
        p.setEtapaId(rs.getLong("cd_etapa"));
        p.setStatus(StatusEtapa.valueOf(rs.getString("ds_status")));
        p.setAtualizadoEm(JdbcUtil.getDataHora(rs, "dt_atualizacao"));
        return p;
    }
}
