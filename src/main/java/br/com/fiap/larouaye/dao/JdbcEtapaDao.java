
package br.com.fiap.larouaye.dao;

import br.com.fiap.larouaye.exception.PersistenciaException;
import br.com.fiap.larouaye.model.Etapa;
import br.com.fiap.larouaye.model.EtapaDocumento;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * DAO da etapa com JDBC puro e transação manual.
 * Adaptado ao modelo relacional definitivo do LarOuAyê.
 */
@Repository
public class JdbcEtapaDao implements EtapaDao {

    private static final String SELECT =
            "SELECT cd_etapa, cd_codigo, nr_ordem, "
                    + "nm_etapa, ds_etapa FROM tb_etapa ";

    private final DataSource dataSource;

    public JdbcEtapaDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Etapa inserir(Etapa e) {

        String sql = "INSERT INTO tb_etapa "
                + "(cd_etapa, cd_codigo, nr_ordem, nm_etapa, ds_etapa) "
                + "VALUES (sq_tb_etapa.NEXTVAL, ?, ?, ?, ?)";

        try (Connection con = dataSource.getConnection()) {

            con.setAutoCommit(false);

            try (PreparedStatement ps = con.prepareStatement(
                    sql, new String[]{"CD_ETAPA"})) {

                ps.setString(1, e.getCodigo());
                ps.setInt(2, e.getOrdem());
                ps.setString(3, e.getNome());
                ps.setString(4, e.getDescricao());

                ps.executeUpdate();

                long id;

                try (ResultSet chaves = ps.getGeneratedKeys()) {
                    chaves.next();
                    id = chaves.getLong(1);
                }

                inserirDocumentos(con, id, e);

                con.commit();

                return buscarPorId(id)
                        .orElseThrow(() ->
                                new PersistenciaException(
                                        "inserir etapa", null));

            } catch (SQLException | RuntimeException ex) {

                con.rollback();
                throw ex;

            } finally {

                con.setAutoCommit(true);
            }

        } catch (SQLException ex) {
            throw JdbcUtil.traduzir(ex, "inserir etapa");
        }
    }

    @Override
    public List<Etapa> listar() {

        return consultar(
                SELECT + "ORDER BY nr_ordem",
                null,
                "listar etapas");
    }

    @Override
    public Optional<Etapa> buscarPorId(Long id) {

        List<Etapa> lista = consultar(
                SELECT + "WHERE cd_etapa = ?",
                id,
                "buscar etapa");

        return lista.isEmpty()
                ? Optional.empty()
                : Optional.of(lista.get(0));
    }

    @Override
    public Optional<Etapa> buscarPorCodigo(String codigo) {

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     SELECT + "WHERE cd_codigo = ?")) {

            ps.setString(1, codigo);

            List<Etapa> lista = new ArrayList<>();

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }

            carregarDocumentos(con, lista);

            return lista.isEmpty()
                    ? Optional.empty()
                    : Optional.of(lista.get(0));

        } catch (SQLException ex) {
            throw JdbcUtil.traduzir(
                    ex, "buscar etapa por código");
        }
    }

    private List<Etapa> consultar(
            String sql,
            Long id,
            String operacao) {

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            if (id != null) {
                ps.setLong(1, id);
            }

            List<Etapa> lista = new ArrayList<>();

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }

            carregarDocumentos(con, lista);

            return lista;

        } catch (SQLException ex) {
            throw JdbcUtil.traduzir(ex, operacao);
        }
    }

    /**
     * Carrega os documentos de todas as etapas em uma consulta,
     * evitando o problema N+1.
     */
    private void carregarDocumentos(
            Connection con,
            List<Etapa> etapas) throws SQLException {

        if (etapas.isEmpty()) {
            return;
        }

        Map<Long, Etapa> porId = new LinkedHashMap<>();

        for (Etapa e : etapas) {
            porId.put(e.getId(), e);
        }

        String marcadores = String.join(", ",
                java.util.Collections.nCopies(
                        porId.size(), "?"));

        String sql = "SELECT cd_documento, cd_etapa, "
                + "nm_documento, ds_observacao "
                + "FROM tb_etapa_documento "
                + "WHERE cd_etapa IN (" + marcadores + ") "
                + "ORDER BY cd_etapa, cd_documento";

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            int i = 1;

            for (Long id : porId.keySet()) {
                ps.setLong(i++, id);
            }

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    porId.get(rs.getLong("cd_etapa"))
                            .getDocumentos()
                            .add(mapearDocumento(rs));
                }
            }
        }
    }

    @Override
    public boolean existeCodigo(String codigo, Long ignorarId) {

        return contar(
                "SELECT COUNT(*) FROM tb_etapa "
                        + "WHERE cd_codigo = ?"
                        + (ignorarId == null
                        ? "" : " AND cd_etapa <> ?"),
                codigo,
                null,
                ignorarId,
                "verificar código da etapa") > 0;
    }

    @Override
    public boolean existeOrdem(int ordem, Long ignorarId) {

        return contar(
                "SELECT COUNT(*) FROM tb_etapa "
                        + "WHERE nr_ordem = ?"
                        + (ignorarId == null
                        ? "" : " AND cd_etapa <> ?"),
                null,
                (long) ordem,
                ignorarId,
                "verificar ordem da etapa") > 0;
    }

    @Override
    public boolean possuiProgresso(Long id) {

        return contar(
                "SELECT COUNT(*) FROM tb_cidadao_etapa "
                        + "WHERE cd_etapa = ?",
                null,
                id,
                null,
                "verificar progresso da etapa") > 0;
    }

    private int contar(
            String sql,
            String texto,
            Long numero,
            Long ignorarId,
            String operacao) {

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            int pos = 1;

            if (texto != null) {
                ps.setString(pos++, texto);
            } else {
                ps.setLong(pos++, numero);
            }

            if (ignorarId != null) {
                ps.setLong(pos, ignorarId);
            }

            try (ResultSet rs = ps.executeQuery()) {

                rs.next();

                return rs.getInt(1);
            }

        } catch (SQLException ex) {
            throw JdbcUtil.traduzir(ex, operacao);
        }
    }

    @Override
    public boolean atualizar(Etapa e) {

        String sql = "UPDATE tb_etapa SET "
                + "cd_codigo = ?, nr_ordem = ?, "
                + "nm_etapa = ?, ds_etapa = ? "
                + "WHERE cd_etapa = ?";

        try (Connection con = dataSource.getConnection()) {

            con.setAutoCommit(false);

            try (PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setString(1, e.getCodigo());
                ps.setInt(2, e.getOrdem());
                ps.setString(3, e.getNome());
                ps.setString(4, e.getDescricao());
                ps.setLong(5, e.getId());

                if (ps.executeUpdate() == 0) {

                    con.rollback();

                    return false;
                }

                apagar(con,
                        "DELETE FROM tb_etapa_documento "
                                + "WHERE cd_etapa = ?",
                        e.getId());

                inserirDocumentos(con, e.getId(), e);

                con.commit();

                return true;

            } catch (SQLException | RuntimeException ex) {

                con.rollback();
                throw ex;

            } finally {

                con.setAutoCommit(true);
            }

        } catch (SQLException ex) {
            throw JdbcUtil.traduzir(ex, "atualizar etapa");
        }
    }

    /**
     * Remove uma etapa e seus vínculos dependentes.
     * Preserva os serviços e as orientações compartilhados.
     * Todas as exclusões ocorrem na mesma transação.
     */
    @Override
    public boolean remover(Long id) {

        try (Connection con = dataSource.getConnection()) {

            con.setAutoCommit(false);

            try {

                apagar(con,
                        "DELETE FROM tb_etapa_documento "
                                + "WHERE cd_etapa = ?", id);

                apagar(con,
                        "DELETE FROM tb_etapa_orientacao "
                                + "WHERE cd_etapa = ?", id);

                // Novo relacionamento N:N com serviços.
                apagar(con,
                        "DELETE FROM tb_etapa_servico "
                                + "WHERE cd_etapa = ?", id);

                int linhas = apagar(con,
                        "DELETE FROM tb_etapa "
                                + "WHERE cd_etapa = ?", id);

                con.commit();

                return linhas > 0;

            } catch (SQLException | RuntimeException ex) {

                con.rollback();
                throw ex;

            } finally {

                con.setAutoCommit(true);
            }

        } catch (SQLException ex) {
            throw JdbcUtil.traduzir(ex, "remover etapa");
        }
    }

    @Override
    public List<EtapaDocumento> listarDocumentos(Long etapaId) {

        String sql = "SELECT cd_documento, cd_etapa, "
                + "nm_documento, ds_observacao "
                + "FROM tb_etapa_documento "
                + "WHERE cd_etapa = ? "
                + "ORDER BY cd_documento";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, etapaId);

            List<EtapaDocumento> lista = new ArrayList<>();

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    lista.add(mapearDocumento(rs));
                }
            }

            return lista;

        } catch (SQLException ex) {
            throw JdbcUtil.traduzir(
                    ex, "listar documentos da etapa");
        }
    }

    private static void inserirDocumentos(
            Connection con,
            long etapaId,
            Etapa e) throws SQLException {

        String sql = "INSERT INTO tb_etapa_documento "
                + "(cd_documento, cd_etapa, "
                + "nm_documento, ds_observacao) "
                + "VALUES (sq_tb_etapa_documento.NEXTVAL, ?, ?, ?)";

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            for (EtapaDocumento d : e.getDocumentos()) {

                ps.setLong(1, etapaId);
                ps.setString(2, d.getNome());

                JdbcUtil.setString(
                        ps, 3, d.getObservacao());

                ps.addBatch();
            }

            ps.executeBatch();
        }
    }

    private static int apagar(
            Connection con,
            String sql,
            long id) throws SQLException {

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, id);

            return ps.executeUpdate();
        }
    }

    private static Etapa mapear(ResultSet rs)
            throws SQLException {

        Etapa e = new Etapa();

        e.setId(rs.getLong("cd_etapa"));
        e.setCodigo(rs.getString("cd_codigo"));
        e.setOrdem(rs.getInt("nr_ordem"));
        e.setNome(rs.getString("nm_etapa"));
        e.setDescricao(rs.getString("ds_etapa"));
        e.setDocumentos(new ArrayList<>());

        return e;
    }

    private static EtapaDocumento mapearDocumento(ResultSet rs)
            throws SQLException {

        EtapaDocumento d = new EtapaDocumento();

        d.setId(rs.getLong("cd_documento"));
        d.setNome(rs.getString("nm_documento"));
        d.setObservacao(rs.getString("ds_observacao"));

        return d;
    }
}
