package br.com.fiap.larouaye.dao;

import br.com.fiap.larouaye.model.Servico;
import br.com.fiap.larouaye.model.TipoServico;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** DAO somente-leitura do guia de serviços (tb_servico), filtrado por UF. */
@Repository
public class JdbcServicoDao implements ServicoDao {

    private final DataSource dataSource;

    public JdbcServicoDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public List<Servico> listarPorUf(String uf) {
        String sql = "SELECT cd_servico, nm_servico, ds_tipo, sg_uf, ds_url, ds_descricao FROM tb_servico "
                + "WHERE sg_uf = ? ORDER BY ds_tipo, nm_servico";
        try (Connection con = dataSource.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, uf);
            List<Servico> lista = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Servico s = new Servico();
                    s.setId(rs.getLong("cd_servico"));
                    s.setNome(rs.getString("nm_servico"));
                    s.setTipo(TipoServico.valueOf(rs.getString("ds_tipo")));
                    s.setUf(rs.getString("sg_uf"));
                    s.setUrl(rs.getString("ds_url"));
                    s.setDescricao(rs.getString("ds_descricao"));
                    lista.add(s);
                }
            }
            return lista;
        } catch (SQLException e) {
            throw JdbcUtil.traduzir(e, "listar serviços");
        }
    }
}
