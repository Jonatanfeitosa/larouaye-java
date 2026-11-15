package br.com.fiap.larouaye.controller;

import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

/** Monta o cabeçalho Location (RFC 9110) apontando para o recurso recém-criado. */
final class Locais {

    private Locais() {
    }

    /** Ex.: requisição POST /api/categorias + id 7 => /api/categorias/7 */
    static URI novoRecurso(Object id) {
        return ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(id).toUri();
    }
}
