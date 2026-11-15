package br.com.fiap.larouaye.controller;

import br.com.fiap.larouaye.dto.CategoriaRequest;
import br.com.fiap.larouaye.dto.CategoriaResponse;
import br.com.fiap.larouaye.dto.OrientacaoResponse;
import br.com.fiap.larouaye.service.CategoriaService;
import br.com.fiap.larouaye.service.OrientacaoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** CRUD das categorias jurídicas + rota aninhada 1:N das orientações de uma categoria. */
@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

    private final CategoriaService service;
    private final OrientacaoService orientacaoService;

    public CategoriaController(CategoriaService service, OrientacaoService orientacaoService) {
        this.service = service;
        this.orientacaoService = orientacaoService;
    }

    @PostMapping
    public ResponseEntity<CategoriaResponse> criar(@Valid @RequestBody CategoriaRequest req) {
        CategoriaResponse criada = service.criar(req);
        return ResponseEntity.created(Locais.novoRecurso(criada.getId())).body(criada);
    }

    @GetMapping
    public List<CategoriaResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public CategoriaResponse buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @GetMapping("/{id}/orientacoes")
    public List<OrientacaoResponse> orientacoes(@PathVariable Long id) {
        return orientacaoService.listarPorCategoria(id);
    }

    @PutMapping("/{id}")
    public CategoriaResponse atualizar(@PathVariable Long id, @Valid @RequestBody CategoriaRequest req) {
        return service.atualizar(id, req);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        service.remover(id);
        return ResponseEntity.noContent().build();
    }
}
