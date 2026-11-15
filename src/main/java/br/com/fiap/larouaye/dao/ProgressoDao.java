package br.com.fiap.larouaye.dao;

import br.com.fiap.larouaye.model.Progresso;

import java.util.List;

/** Acesso ao banco para tb_cidadao_etapa (progresso, relação N:N). */
public interface ProgressoDao {

    List<Progresso> listarPorCidadao(Long cidadaoId);

    /** Atualiza o status; se ainda não existir a linha, insere. Devolve o registro gravado. */
    Progresso salvar(Progresso progresso);
}
