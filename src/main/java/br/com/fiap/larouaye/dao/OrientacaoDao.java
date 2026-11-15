package br.com.fiap.larouaye.dao;

import br.com.fiap.larouaye.model.Orientacao;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/** Acesso ao banco para tb_orientacao e seus filhos (tb_cuidado e tb_fonte_legal). A orientação é a raiz do agregado. */
public interface OrientacaoDao {

    Orientacao inserir(Orientacao orientacao);

    List<Orientacao> listar();

    List<Orientacao> listarPorCategoria(Long categoriaId);

    /** Somente orientações ativas das categorias informadas (usado para responder uma consulta). */
    List<Orientacao> listarAtivasPorCategorias(Collection<Long> categoriaIds);

    /** Rota N:N: orientações que ajudam em uma etapa (tb_etapa_orientacao). */
    List<Orientacao> listarPorEtapa(Long etapaId);

    Optional<Orientacao> buscarPorId(Long id);

    boolean atualizar(Orientacao orientacao);

    /** Remove a orientação, seus cuidados, fontes e vínculos com etapas. */
    boolean remover(Long id);

    /** A orientação já foi entregue em alguma consulta? (impede a remoção). */
    boolean possuiConsultas(Long id);
}
