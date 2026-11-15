package br.com.fiap.larouaye.dao;

import br.com.fiap.larouaye.exception.PersistenciaException;
import br.com.fiap.larouaye.model.FonteLegal;
import br.com.fiap.larouaye.model.Orientacao;
import br.com.fiap.larouaye.model.StatusRevisao;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * DAO da orientação (raiz do agregado) com JDBC puro. Grava e lê junto os cuidados e as fontes legais,
 * e usa TRANSAÇÃO manual (commit/rollback) para o agregado nunca ficar pela metade.
 */
@Repository
public class JdbcOrientacaoDao implements OrientacaoDao {

    private static final int LOTE_IN = 500; // o Oracle aceita até 1000 itens em IN (...)

    private static final String SELECT = "SELECT o.cd_orientacao, o.cd_categoria, c.cd_codigo, o.nm_titulo, o.ds_pergunta, "
            + "o.ds_conteudo, o.ds_url_video, o.ds_revisao, o.dt_revisao, o.st_ativo "
            + "FROM tb_orientacao o INNER JOIN tb_categoria c ON c.cd_categoria = o.cd_categoria ";

    private final DataSource dataSource;

    public JdbcOrientacaoDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // ------------------------------------------------------------ create
    @Override
    public Orientacao inserir(Orientacao o) {
        String sql = "INSERT INTO tb_orientacao (cd_orientacao, cd_categoria, nm_titulo, ds_pergunta, ds_conteudo, "
                + "ds_url_video, ds_revisao, dt_revisao, st_ativo) VALUES (sq_tb_orientacao.NEXTVAL, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = dataSource.getConnection()) {
            con.setAutoCommit(false);
            try (PreparedStatement ps = con.prepareStatement(sql, new String[] {"CD_ORIENTACAO"})) {
                preencherPrincipal(ps, o);
                ps.executeUpdate();
                long id;
                try (ResultSet chaves = ps.getGeneratedKeys()) {
                    chaves.next();
                    id = chaves.getLong(1);
                }
                inserirFilhos(con, id, o);
                con.commit();
                return buscarPorId(id).orElseThrow(() -> new PersistenciaException("inserir orientação", null));
            } catch (SQLException | RuntimeException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw JdbcUtil.traduzir(e, "inserir orientação");
        }
    }

    // ------------------------------------------------------------ read
    @Override
    public List<Orientacao> listar() {
        return consultar(SELECT + "ORDER BY o.cd_orientacao", null, "listar orientações");
    }

    @Override
    public List<Orientacao> listarPorCategoria(Long categoriaId) {
        return consultar(SELECT + "WHERE o.cd_categoria = ? ORDER BY o.cd_orientacao", List.of(categoriaId), "listar orientações da categoria");
    }

    @Override
    public List<Orientacao> listarAtivasPorCategorias(Collection<Long> categoriaIds) {
        if (categoriaIds.isEmpty()) {
            return List.of();
        }
        String marcadores = String.join(", ", java.util.Collections.nCopies(categoriaIds.size(), "?"));
        return consultar(SELECT + "WHERE o.st_ativo = 1 AND o.cd_categoria IN (" + marcadores + ") ORDER BY o.cd_orientacao",
                new ArrayList<>(categoriaIds), "listar orientações ativas");
    }

    @Override
    public List<Orientacao> listarPorEtapa(Long etapaId) {
        return consultar(SELECT + "INNER JOIN tb_etapa_orientacao eo ON eo.cd_orientacao = o.cd_orientacao "
                + "WHERE eo.cd_etapa = ? ORDER BY o.cd_orientacao", List.of(etapaId), "listar orientações da etapa");
    }

    @Override
    public Optional<Orientacao> buscarPorId(Long id) {
        List<Orientacao> lista = consultar(SELECT + "WHERE o.cd_orientacao = ?", List.of(id), "buscar orientação");
        return lista.isEmpty() ? Optional.empty() : Optional.of(lista.get(0));
    }

    /** Executa um SELECT com parâmetros numéricos e já carrega os filhos de todas as orientações de uma vez. */
    private List<Orientacao> consultar(String sql, List<Long> parametros, String operacao) {
        try (Connection con = dataSource.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            if (parametros != null) {
                for (int i = 0; i < parametros.size(); i++) {
                    ps.setLong(i + 1, parametros.get(i));
                }
            }
            List<Orientacao> lista = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
            carregarFilhos(con, lista);
            return lista;
        } catch (SQLException e) {
            throw JdbcUtil.traduzir(e, operacao);
        }
    }

    private void carregarFilhos(Connection con, List<Orientacao> orientacoes) throws SQLException {
        Map<Long, Orientacao> porId = new LinkedHashMap<>();
        for (Orientacao o : orientacoes) {
            porId.put(o.getId(), o);
        }
        List<Long> ids = new ArrayList<>(porId.keySet());
        for (int inicio = 0; inicio < ids.size(); inicio += LOTE_IN) {
            List<Long> lote = ids.subList(inicio, Math.min(inicio + LOTE_IN, ids.size()));
            String marcadores = String.join(", ", java.util.Collections.nCopies(lote.size(), "?"));

            String sqlCuidados = "SELECT cd_orientacao, ds_cuidado FROM tb_cuidado WHERE cd_orientacao IN (" + marcadores
                    + ") ORDER BY cd_orientacao, nr_ordem";
            try (PreparedStatement ps = con.prepareStatement(sqlCuidados)) {
                for (int i = 0; i < lote.size(); i++) {
                    ps.setLong(i + 1, lote.get(i));
                }
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        porId.get(rs.getLong("cd_orientacao")).getCuidados().add(rs.getString("ds_cuidado"));
                    }
                }
            }

            String sqlFontes = "SELECT cd_fonte, cd_orientacao, nm_norma, ds_referencia, ds_url FROM tb_fonte_legal "
                    + "WHERE cd_orientacao IN (" + marcadores + ") ORDER BY cd_orientacao, cd_fonte";
            try (PreparedStatement ps = con.prepareStatement(sqlFontes)) {
                for (int i = 0; i < lote.size(); i++) {
                    ps.setLong(i + 1, lote.get(i));
                }
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        FonteLegal f = new FonteLegal();
                        f.setId(rs.getLong("cd_fonte"));
                        f.setNorma(rs.getString("nm_norma"));
                        f.setReferencia(rs.getString("ds_referencia"));
                        f.setUrl(rs.getString("ds_url"));
                        porId.get(rs.getLong("cd_orientacao")).getBaseLegal().add(f);
                    }
                }
            }
        }
    }

    // ------------------------------------------------------------ update
    @Override
    public boolean atualizar(Orientacao o) {
        String sql = "UPDATE tb_orientacao SET cd_categoria = ?, nm_titulo = ?, ds_pergunta = ?, ds_conteudo = ?, "
                + "ds_url_video = ?, ds_revisao = ?, dt_revisao = ?, st_ativo = ? WHERE cd_orientacao = ?";
        try (Connection con = dataSource.getConnection()) {
            con.setAutoCommit(false);
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                preencherPrincipal(ps, o);
                ps.setLong(9, o.getId());
                if (ps.executeUpdate() == 0) {
                    con.rollback();
                    return false;
                }
                apagarFilhos(con, o.getId());
                inserirFilhos(con, o.getId(), o);
                con.commit();
                return true;
            } catch (SQLException | RuntimeException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw JdbcUtil.traduzir(e, "atualizar orientação");
        }
    }

    // ------------------------------------------------------------ delete
    @Override
    public boolean remover(Long id) {
        try (Connection con = dataSource.getConnection()) {
            con.setAutoCommit(false);
            try {
                apagarFilhos(con, id);
                apagar(con, "DELETE FROM tb_etapa_orientacao WHERE cd_orientacao = ?", id);
                int linhas = apagar(con, "DELETE FROM tb_orientacao WHERE cd_orientacao = ?", id);
                con.commit();
                return linhas > 0;
            } catch (SQLException | RuntimeException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw JdbcUtil.traduzir(e, "remover orientação");
        }
    }

    @Override
    public boolean possuiConsultas(Long id) {
        String sql = "SELECT COUNT(*) FROM tb_consulta WHERE cd_orientacao = ?";
        try (Connection con = dataSource.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw JdbcUtil.traduzir(e, "verificar consultas da orientação");
        }
    }

    // ------------------------------------------------------------ apoio
    private static void preencherPrincipal(PreparedStatement ps, Orientacao o) throws SQLException {
        ps.setLong(1, o.getCategoriaId());
        ps.setString(2, o.getTitulo());
        ps.setString(3, o.getPerguntaRelacionada());
        ps.setString(4, o.getConteudo());
        JdbcUtil.setString(ps, 5, o.getUrlVideo());
        ps.setString(6, o.getRevisao().name());
        JdbcUtil.setData(ps, 7, o.getDataRevisao());
        ps.setInt(8, o.isAtivo() ? 1 : 0);
    }

    private static void inserirFilhos(Connection con, long orientacaoId, Orientacao o) throws SQLException {
        String sqlCuidado = "INSERT INTO tb_cuidado (cd_cuidado, cd_orientacao, nr_ordem, ds_cuidado) VALUES (sq_tb_cuidado.NEXTVAL, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sqlCuidado)) {
            int ordem = 1;
            for (String cuidado : o.getCuidados()) {
                ps.setLong(1, orientacaoId);
                ps.setInt(2, ordem++);
                ps.setString(3, cuidado);
                ps.addBatch();
            }
            ps.executeBatch();
        }
        String sqlFonte = "INSERT INTO tb_fonte_legal (cd_fonte, cd_orientacao, nm_norma, ds_referencia, ds_url) VALUES (sq_tb_fonte_legal.NEXTVAL, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sqlFonte)) {
            for (FonteLegal f : o.getBaseLegal()) {
                ps.setLong(1, orientacaoId);
                ps.setString(2, f.getNorma());
                JdbcUtil.setString(ps, 3, f.getReferencia());
                JdbcUtil.setString(ps, 4, f.getUrl());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private static void apagarFilhos(Connection con, long orientacaoId) throws SQLException {
        apagar(con, "DELETE FROM tb_cuidado WHERE cd_orientacao = ?", orientacaoId);
        apagar(con, "DELETE FROM tb_fonte_legal WHERE cd_orientacao = ?", orientacaoId);
    }

    private static int apagar(Connection con, String sql, long id) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate();
        }
    }

    private static Orientacao mapear(ResultSet rs) throws SQLException {
        Orientacao o = new Orientacao();
        o.setId(rs.getLong("cd_orientacao"));
        o.setCategoriaId(rs.getLong("cd_categoria"));
        o.setCategoriaCodigo(rs.getString("cd_codigo"));
        o.setTitulo(rs.getString("nm_titulo"));
        o.setPerguntaRelacionada(rs.getString("ds_pergunta"));
        o.setConteudo(rs.getString("ds_conteudo"));
        o.setUrlVideo(rs.getString("ds_url_video"));
        o.setRevisao(StatusRevisao.valueOf(rs.getString("ds_revisao")));
        o.setDataRevisao(JdbcUtil.getData(rs, "dt_revisao"));
        o.setAtivo(rs.getInt("st_ativo") == 1);
        o.setCuidados(new ArrayList<>());
        o.setBaseLegal(new ArrayList<>());
        return o;
    }
}
