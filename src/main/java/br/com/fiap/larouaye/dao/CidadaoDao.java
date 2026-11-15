package br.com.fiap.larouaye.dao;

import br.com.fiap.larouaye.model.Cidadao;

import java.util.List;
import java.util.Optional;

/** Acesso ao banco para tb_cidadao. */
public interface CidadaoDao {

    Cidadao inserir(Cidadao cidadao);

    List<Cidadao> listar();

    Optional<Cidadao> buscarPorId(Long id);

    /** Existe outro cidadão com este e-mail? (`ignorarId` evita comparar com ele mesmo; pode ser nulo). */
    boolean existeEmail(String email, Long ignorarId);

    /** Existe cidadão com este canal e id externo? (Telegram). */
    boolean existeCanalExterno(String canal, String idExterno, Long ignorarId);

    boolean atualizar(Cidadao cidadao);

    boolean remover(Long id);

    /** O cidadão tem consultas ou progresso registrados? (impede a remoção). */
    boolean possuiRegistros(Long id);
}
