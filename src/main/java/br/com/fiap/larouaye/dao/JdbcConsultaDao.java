package br.com.fiap.larouaye.dao;

import br.com.fiap.larouaye.exception.PersistenciaException;
import br.com.fiap.larouaye.model.Canal;
import br.com.fiap.larouaye.model.Consulta;
import br.com.fiap.larouaye.model.StatusConsulta;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** DAO da consulta (lado N do 1:N com o cidadão) com JDBC puro. */
@Repository
public class JdbcConsultaDao implements ConsultaDao {

    private static final String SELECT = "SELECT q.cd_consulta, q.cd_cidadao, q.cd_categoria, c.cd_codigo, q.cd_orientacao, "
            + "q.ds_pergunta, q.sg_uf, q.ds_etapa_atual, q.vl_confianca, q.ds_status, q.ds_canal, q.dt_consulta "
            + "FROM tb_consulta q LEFT JOIN tb_categoria c ON c.cd_categoria = q.cd_categoria ";

    private final DataSource dataSource;

    public JdbcConsultaDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Consulta inserir(Consulta q) {
        String sql = "INSERT INTO tb_consulta (cd_consulta, cd_cidadao, cd_categoria, cd_orientacao, ds_pergunta, sg_uf, "
                + "ds_etapa_atual, vl_confianca, ds_status, ds_canal) VALUES (sq_tb_consulta.NEXTVAL, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = dataSource.getConnection(); PreparedStatement ps = con.prepareStatement(sql, new String[] {"CD_CONSULTA"})) {
            preencher(ps, q);
            ps.executeUpdate();
            long id;
            try (ResultSet chaves = ps.getGeneratedKeys()) {
                chaves.next();
                id = chaves.getLong(1);
            }
            return buscarPorId(id).orElseThrow(() -> new PersistenciaException("inserir consulta", null));
        } catch (SQLException e) {
            throw JdbcUtil.traduzir(e, "inserir consulta");
        }
    }

    @Override
    public List<Consulta> listarPorCidadao(Long cidadaoId) {
        String sql = SELECT + "WHERE q.cd_cidadao = ? ORDER BY q.dt_consulta DESC, q.cd_consulta DESC";
        try (Connection con = dataSource.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, cidadaoId);
            List<Consulta> lista = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
            return lista;
        } catch (SQLException e) {
            throw JdbcUtil.traduzir(e, "listar consultas do cidadão");
        }
    }

    @Override
    public Optional<Consulta> buscarPorId(Long id) {
        try (Connection con = dataSource.getConnection(); PreparedStatement ps = con.prepareStatement(SELECT + "WHERE q.cd_consulta = ?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw JdbcUtil.traduzir(e, "buscar consulta");
        }
    }

    @Override
    public boolean atualizar(Consulta q) {
        String sql = "UPDATE tb_consulta SET cd_cidadao = ?, cd_categoria = ?, cd_orientacao = ?, ds_pergunta = ?, sg_uf = ?, "
                + "ds_etapa_atual = ?, vl_confianca = ?, ds_status = ?, ds_canal = ? WHERE cd_consulta = ?";
        try (Connection con = dataSource.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            preencher(ps, q);
            ps.setLong(10, q.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw JdbcUtil.traduzir(e, "atualizar consulta");
        }
    }

    @Override
    public boolean remover(Long id) {
        try (Connection con = dataSource.getConnection(); PreparedStatement ps = con.prepareStatement("DELETE FROM tb_consulta WHERE cd_consulta = ?")) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw JdbcUtil.traduzir(e, "remover consulta");
        }
    }

    private static void preencher(PreparedStatement ps, Consulta q) throws SQLException {
        ps.setLong(1, q.getCidadaoId());
        JdbcUtil.setLong(ps, 2, q.getCategoriaId());
        JdbcUtil.setLong(ps, 3, q.getOrientacaoId());
        ps.setString(4, q.getPergunta());
        ps.setString(5, q.getUf());
        JdbcUtil.setString(ps, 6, q.getEtapaAtual());
        if (q.getConfianca() == null) {
            ps.setNull(7, Types.NUMERIC);
        } else {
            ps.setBigDecimal(7, q.getConfianca());
        }
        ps.setString(8, q.getStatus().name());
        ps.setString(9, q.getCanal().name());
    }

    private static Consulta mapear(ResultSet rs) throws SQLException {
        Consulta q = new Consulta();
        q.setId(rs.getLong("cd_consulta"));
        q.setCidadaoId(rs.getLong("cd_cidadao"));
        q.setCategoriaId(JdbcUtil.getLong(rs, "cd_categoria"));
        q.setCategoriaCodigo(rs.getString("cd_codigo"));
        q.setOrientacaoId(JdbcUtil.getLong(rs, "cd_orientacao"));
        q.setPergunta(rs.getString("ds_pergunta"));
        q.setUf(rs.getString("sg_uf"));
        q.setEtapaAtual(rs.getString("ds_etapa_atual"));
        BigDecimal conf = rs.getBigDecimal("vl_confianca");
        q.setConfianca(conf);
        q.setStatus(StatusConsulta.valueOf(rs.getString("ds_status")));
        q.setCanal(Canal.valueOf(rs.getString("ds_canal")));
        q.setDataConsulta(JdbcUtil.getDataHora(rs, "dt_consulta"));
        return q;
    }
}
