package br.com.fiap.larouaye.controller;

import br.com.fiap.larouaye.dto.EtapaDocumentoResponse;
import br.com.fiap.larouaye.dto.EtapaRequest;
import br.com.fiap.larouaye.dto.EtapaResponse;
import br.com.fiap.larouaye.service.EtapaService;
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

/** CRUD das etapas da compra + rota aninhada 1:N dos documentos de uma etapa. */
@RestController
@RequestMapping("/api/etapas")
public class EtapaController {

    private final EtapaService service;

    public EtapaController(EtapaService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<EtapaResponse> criar(@Valid @RequestBody EtapaRequest req) {
        EtapaResponse criada = service.criar(req);
        return ResponseEntity.created(Locais.novoRecurso(criada.getId())).body(criada);
    }

    @GetMapping
    public List<EtapaResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public EtapaResponse buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @GetMapping("/{id}/documentos")
    public List<EtapaDocumentoResponse> documentos(@PathVariable Long id) {
        return service.listarDocumentos(id);
    }

    @PutMapping("/{id}")
    public EtapaResponse atualizar(@PathVariable Long id, @Valid @RequestBody EtapaRequest req) {
        return service.atualizar(id, req);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        service.remover(id);
        return ResponseEntity.noContent().build();
    }
}
