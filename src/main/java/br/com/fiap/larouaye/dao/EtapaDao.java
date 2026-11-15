package br.com.fiap.larouaye.dao;

import br.com.fiap.larouaye.model.Etapa;
import br.com.fiap.larouaye.model.EtapaDocumento;

import java.util.List;
import java.util.Optional;

/** Acesso ao banco para tb_etapa e seus documentos (tb_etapa_documento). A etapa é a raiz do agregado. */
public interface EtapaDao {

    Etapa inserir(Etapa etapa);

    List<Etapa> listar();

    Optional<Etapa> buscarPorId(Long id);

    Optional<Etapa> buscarPorCodigo(String codigo);

    boolean existeCodigo(String codigo, Long ignorarId);

    boolean existeOrdem(int ordem, Long ignorarId);

    boolean atualizar(Etapa etapa);

    /** Remove a etapa, seus documentos e vínculos com orientações. */
    boolean remover(Long id);

    /** Algum cidadão tem progresso nesta etapa? (impede a remoção). */
    boolean possuiProgresso(Long id);

    /** Rota aninhada 1:N: documentos de uma etapa. */
    List<EtapaDocumento> listarDocumentos(Long etapaId);
}
