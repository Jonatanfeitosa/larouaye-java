
package br.com.fiap.larouaye.dao;

import br.com.fiap.larouaye.exception.PersistenciaException;
import br.com.fiap.larouaye.model.Canal;
import br.com.fiap.larouaye.model.Cidadao;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO de cidadão com JDBC puro.
 * Adaptado ao relacionamento com TB_CANAL.
 */
@Repository
public class JdbcCidadaoDao implements CidadaoDao {

    private static final String COLUNAS =
            "cd_cidadao, nm_cidadao, ds_email, "
                    + "(SELECT cn.cd_codigo FROM tb_canal cn "
                    + "WHERE cn.cd_canal = tb_cidadao.cd_canal) AS ds_canal, "
                    + "cd_id_externo, dt_cadastro";

    private final DataSource dataSource;

    public JdbcCidadaoDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Cidadao inserir(Cidadao c) {
        String sql = "INSERT INTO tb_cidadao "
                + "(cd_cidadao, nm_cidadao, ds_email, cd_canal, cd_id_externo) "
                + "VALUES (sq_tb_cidadao.NEXTVAL, ?, ?, "
                + "(SELECT cd_canal FROM tb_canal WHERE cd_codigo = ?), ?)";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     sql, new String[]{"CD_CIDADAO"})) {

            ps.setString(1, c.getNome());
            JdbcUtil.setString(ps, 2, c.getEmail());
            ps.setString(3, c.getCanal().name());
            JdbcUtil.setString(ps, 4, c.getIdExterno());

            ps.executeUpdate();

            try (ResultSet chaves = ps.getGeneratedKeys()) {
                chaves.next();

                return buscarPorId(chaves.getLong(1))
                        .orElseThrow(() ->
                                new PersistenciaException("inserir cidadão", null));
            }

        } catch (SQLException e) {
            throw JdbcUtil.traduzir(e, "inserir cidadão");
        }
    }

    @Override
    public List<Cidadao> listar() {
        String sql = "SELECT " + COLUNAS
                + " FROM tb_cidadao ORDER BY cd_cidadao";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            List<Cidadao> lista = new ArrayList<>();

            while (rs.next()) {
                lista.add(mapear(rs));
            }

            return lista;

        } catch (SQLException e) {
            throw JdbcUtil.traduzir(e, "listar cidadãos");
        }
    }

    @Override
    public Optional<Cidadao> buscarPorId(Long id) {
        String sql = "SELECT " + COLUNAS
                + " FROM tb_cidadao WHERE cd_cidadao = ?";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next()
                        ? Optional.of(mapear(rs))
                        : Optional.empty();
            }

        } catch (SQLException e) {
            throw JdbcUtil.traduzir(e, "buscar cidadão");
        }
    }

    @Override
    public boolean existeEmail(String email, Long ignorarId) {
        String sql = "SELECT COUNT(*) FROM tb_cidadao "
                + "WHERE LOWER(ds_email) = LOWER(?)"
                + (ignorarId == null ? "" : " AND cd_cidadao <> ?");

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, email);

            if (ignorarId != null) {
                ps.setLong(2, ignorarId);
            }

            return contar(ps) > 0;

        } catch (SQLException e) {
            throw JdbcUtil.traduzir(e, "verificar e-mail");
        }
    }

    @Override
    public boolean existeCanalExterno(
            String canal, String idExterno, Long ignorarId) {

        String sql = "SELECT COUNT(*) FROM tb_cidadao "
                + "WHERE cd_canal = "
                + "(SELECT cd_canal FROM tb_canal WHERE cd_codigo = ?) "
                + "AND cd_id_externo = ?"
                + (ignorarId == null ? "" : " AND cd_cidadao <> ?");

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, canal);
            ps.setString(2, idExterno);

            if (ignorarId != null) {
                ps.setLong(3, ignorarId);
            }

            return contar(ps) > 0;

        } catch (SQLException e) {
            throw JdbcUtil.traduzir(e, "verificar canal externo");
        }
    }

    @Override
    public boolean atualizar(Cidadao c) {
        String sql = "UPDATE tb_cidadao "
                + "SET nm_cidadao = ?, ds_email = ?, "
                + "cd_canal = (SELECT cd_canal FROM tb_canal "
                + "WHERE cd_codigo = ?), "
                + "cd_id_externo = ? WHERE cd_cidadao = ?";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, c.getNome());
            JdbcUtil.setString(ps, 2, c.getEmail());
            ps.setString(3, c.getCanal().name());
            JdbcUtil.setString(ps, 4, c.getIdExterno());
            ps.setLong(5, c.getId());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw JdbcUtil.traduzir(e, "atualizar cidadão");
        }
    }

    @Override
    public boolean remover(Long id) {
        String sql = "DELETE FROM tb_cidadao WHERE cd_cidadao = ?";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, id);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw JdbcUtil.traduzir(e, "remover cidadão");
        }
    }

    @Override
    public boolean possuiRegistros(Long id) {
        String sql = "SELECT "
                + "(SELECT COUNT(*) FROM tb_consulta WHERE cd_cidadao = ?) "
                + "+ (SELECT COUNT(*) FROM tb_cidadao_etapa WHERE cd_cidadao = ?) "
                + "FROM dual";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, id);
            ps.setLong(2, id);

            return contar(ps) > 0;

        } catch (SQLException e) {
            throw JdbcUtil.traduzir(e, "verificar registros do cidadão");
        }
    }

    private static int contar(PreparedStatement ps) throws SQLException {
        try (ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }

    private static Cidadao mapear(ResultSet rs) throws SQLException {
        Cidadao c = new Cidadao();

        c.setId(rs.getLong("cd_cidadao"));
        c.setNome(rs.getString("nm_cidadao"));
        c.setEmail(rs.getString("ds_email"));
        c.setCanal(Canal.valueOf(rs.getString("ds_canal")));
        c.setIdExterno(rs.getString("cd_id_externo"));
        c.setDataCadastro(JdbcUtil.getDataHora(rs, "dt_cadastro"));

        return c;
    }
}
