package br.com.fiap.larouaye.dao;

import br.com.fiap.larouaye.model.Servico;

import java.util.List;

/** Acesso ao banco para tb_servico. */
public interface ServicoDao {

    List<Servico> listarPorUf(String uf);
}
