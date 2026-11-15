package br.com.fiap.larouaye.controller;

import br.com.fiap.larouaye.dto.ProgressoRequest;
import br.com.fiap.larouaye.dto.ProgressoResponse;
import br.com.fiap.larouaye.service.ProgressoService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Rota aninhada: progresso do cidadão nas etapas (relação N:N). POST grava ou atualiza (idempotente). */
@RestController
@RequestMapping({"/api/usuarios/{cidadaoId}/etapas", "/api/cidadaos/{cidadaoId}/etapas"})
public class ProgressoController {

    private final ProgressoService service;

    public ProgressoController(ProgressoService service) {
        this.service = service;
    }

    @GetMapping
    public List<ProgressoResponse> listar(@PathVariable Long cidadaoId) {
        return service.listar(cidadaoId);
    }

    @PostMapping
    public ProgressoResponse salvar(@PathVariable Long cidadaoId, @Valid @RequestBody ProgressoRequest req) {
        return service.salvar(cidadaoId, req);
    }
}
