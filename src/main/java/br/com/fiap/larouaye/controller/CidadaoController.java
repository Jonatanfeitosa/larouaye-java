package br.com.fiap.larouaye.controller;

import br.com.fiap.larouaye.dto.CidadaoRequest;
import br.com.fiap.larouaye.dto.CidadaoResponse;
import br.com.fiap.larouaye.service.CidadaoService;
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

/** CRUD do cidadão. "/api/usuarios" é o caminho que o front usa; "/api/cidadaos" é o nome canônico do domínio. */
@RestController
@RequestMapping({"/api/usuarios", "/api/cidadaos"})
public class CidadaoController {

    private final CidadaoService service;

    public CidadaoController(CidadaoService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<CidadaoResponse> criar(@Valid @RequestBody CidadaoRequest req) {
        CidadaoResponse criado = service.criar(req);
        return ResponseEntity.created(Locais.novoRecurso(criado.getId())).body(criado);
    }

    @GetMapping
    public List<CidadaoResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public CidadaoResponse buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PutMapping("/{id}")
    public CidadaoResponse atualizar(@PathVariable Long id, @Valid @RequestBody CidadaoRequest req) {
        return service.atualizar(id, req);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        service.remover(id);
        return ResponseEntity.noContent().build();
    }
}
