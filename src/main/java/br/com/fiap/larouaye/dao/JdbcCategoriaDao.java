package br.com.fiap.larouaye.dao;

import br.com.fiap.larouaye.exception.PersistenciaException;
import br.com.fiap.larouaye.model.Categoria;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** DAO de categoria com JDBC puro. */
@Repository
public class JdbcCategoriaDao implements CategoriaDao {

    private static final String COLUNAS = "cd_categoria, cd_codigo, nm_categoria, ds_categoria";

    private final DataSource dataSource;

    public JdbcCategoriaDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Categoria inserir(Categoria c) {
        String sql = "INSERT INTO tb_categoria (cd_categoria, cd_codigo, nm_categoria, ds_categoria) VALUES (sq_tb_categoria.NEXTVAL, ?, ?, ?)";
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, new String[] {"CD_CATEGORIA"})) {
            ps.setString(1, c.getCodigo());
            ps.setString(2, c.getNome());
            JdbcUtil.setString(ps, 3, c.getDescricao());
            ps.executeUpdate();
            try (ResultSet chaves = ps.getGeneratedKeys()) {
                chaves.next();
                return buscarPorId(chaves.getLong(1)).orElseThrow(() -> new PersistenciaException("inserir categoria", null));
            }
        } catch (SQLException e) {
            throw JdbcUtil.traduzir(e, "inserir categoria");
        }
    }

    @Override
    public List<Categoria> listar() {
        String sql = "SELECT " + COLUNAS + " FROM tb_categoria ORDER BY cd_categoria";
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Categoria> lista = new ArrayList<>();
            while (rs.next()) {
                lista.add(mapear(rs));
            }
            return lista;
        } catch (SQLException e) {
            throw JdbcUtil.traduzir(e, "listar categorias");
        }
    }

    @Override
    public Optional<Categoria> buscarPorId(Long id) {
        return buscarUma("SELECT " + COLUNAS + " FROM tb_categoria WHERE cd_categoria = ?", id, null, "buscar categoria");
    }

    @Override
    public Optional<Categoria> buscarPorCodigo(String codigo) {
        return buscarUma("SELECT " + COLUNAS + " FROM tb_categoria WHERE cd_codigo = ?", null, codigo, "buscar categoria por código");
    }

    private Optional<Categoria> buscarUma(String sql, Long id, String texto, String operacao) {
        try (Connection con = dataSource.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            if (id != null) {
                ps.setLong(1, id);
            } else {
                ps.setString(1, texto);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw JdbcUtil.traduzir(e, operacao);
        }
    }

    @Override
    public boolean existeCodigo(String codigo, Long ignorarId) {
        String sql = "SELECT COUNT(*) FROM tb_categoria WHERE cd_codigo = ?" + (ignorarId == null ? "" : " AND cd_categoria <> ?");
        try (Connection con = dataSource.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, codigo);
            if (ignorarId != null) {
                ps.setLong(2, ignorarId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw JdbcUtil.traduzir(e, "verificar código da categoria");
        }
    }

    @Override
    public boolean atualizar(Categoria c) {
        String sql = "UPDATE tb_categoria SET cd_codigo = ?, nm_categoria = ?, ds_categoria = ? WHERE cd_categoria = ?";
        try (Connection con = dataSource.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, c.getCodigo());
            ps.setString(2, c.getNome());
            JdbcUtil.setString(ps, 3, c.getDescricao());
            ps.setLong(4, c.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw JdbcUtil.traduzir(e, "atualizar categoria");
        }
    }

    @Override
    public boolean remover(Long id) {
        String sql = "DELETE FROM tb_categoria WHERE cd_categoria = ?";
        try (Connection con = dataSource.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw JdbcUtil.traduzir(e, "remover categoria");
        }
    }

    @Override
    public boolean possuiFilhos(Long id) {
        String sql = "SELECT (SELECT COUNT(*) FROM tb_orientacao WHERE cd_categoria = ?) "
                + "+ (SELECT COUNT(*) FROM tb_consulta WHERE cd_categoria = ?) FROM dual";
        try (Connection con = dataSource.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.setLong(2, id);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw JdbcUtil.traduzir(e, "verificar filhos da categoria");
        }
    }

    private static Categoria mapear(ResultSet rs) throws SQLException {
        Categoria c = new Categoria();
        c.setId(rs.getLong("cd_categoria"));
        c.setCodigo(rs.getString("cd_codigo"));
        c.setNome(rs.getString("nm_categoria"));
        c.setDescricao(rs.getString("ds_categoria"));
        return c;
    }
}
