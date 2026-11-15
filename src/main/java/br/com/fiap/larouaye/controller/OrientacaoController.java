package br.com.fiap.larouaye.controller;

import br.com.fiap.larouaye.dto.OrientacaoRequest;
import br.com.fiap.larouaye.dto.OrientacaoResponse;
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

/** CRUD do catálogo de orientações jurídicas (a base que fundamenta toda resposta). */
@RestController
@RequestMapping("/api/orientacoes")
public class OrientacaoController {

    private final OrientacaoService service;

    public OrientacaoController(OrientacaoService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<OrientacaoResponse> criar(@Valid @RequestBody OrientacaoRequest req) {
        OrientacaoResponse criada = service.criar(req);
        return ResponseEntity.created(Locais.novoRecurso(criada.getId())).body(criada);
    }

    @GetMapping
    public List<OrientacaoResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public OrientacaoResponse buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PutMapping("/{id}")
    public OrientacaoResponse atualizar(@PathVariable Long id, @Valid @RequestBody OrientacaoRequest req) {
        return service.atualizar(id, req);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        service.remover(id);
        return ResponseEntity.noContent().build();
    }
}
