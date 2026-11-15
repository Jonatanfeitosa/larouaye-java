package br.com.fiap.larouaye.dao;

import br.com.fiap.larouaye.model.Consulta;

import java.util.List;
import java.util.Optional;

/** Acesso ao banco para tb_consulta. */
public interface ConsultaDao {

    Consulta inserir(Consulta consulta);

    List<Consulta> listarPorCidadao(Long cidadaoId);

    Optional<Consulta> buscarPorId(Long id);

    boolean atualizar(Consulta consulta);

    boolean remover(Long id);
}
