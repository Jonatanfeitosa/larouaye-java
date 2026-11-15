package br.com.fiap.larouaye.dao;

import br.com.fiap.larouaye.model.Categoria;

import java.util.List;
import java.util.Optional;

/** Acesso ao banco para tb_categoria. */
public interface CategoriaDao {

    Categoria inserir(Categoria categoria);

    List<Categoria> listar();

    Optional<Categoria> buscarPorId(Long id);

    Optional<Categoria> buscarPorCodigo(String codigo);

    boolean existeCodigo(String codigo, Long ignorarId);

    boolean atualizar(Categoria categoria);

    boolean remover(Long id);

    /** A categoria tem orientações ou consultas ligadas? (impede a remoção). */
    boolean possuiFilhos(Long id);
}
