package br.com.fiap.larouaye.controller;

import br.com.fiap.larouaye.dto.ConsultaRequest;
import br.com.fiap.larouaye.dto.ConsultaRespostaResponse;
import br.com.fiap.larouaye.dto.ConsultaResumoResponse;
import br.com.fiap.larouaye.service.ConsultaService;
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

/** Rota aninhada 1:N: as consultas de um cidadão. */
@RestController
@RequestMapping({"/api/usuarios/{cidadaoId}/consultas", "/api/cidadaos/{cidadaoId}/consultas"})
public class ConsultaController {

    private final ConsultaService service;

    public ConsultaController(ConsultaService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ConsultaRespostaResponse> perguntar(@PathVariable Long cidadaoId, @Valid @RequestBody ConsultaRequest req) {
        ConsultaRespostaResponse resposta = service.perguntar(cidadaoId, req);
        return ResponseEntity.created(Locais.novoRecurso(resposta.getConsultaId())).body(resposta);
    }

    @GetMapping
    public List<ConsultaResumoResponse> listar(@PathVariable Long cidadaoId) {
        return service.listar(cidadaoId);
    }

    @GetMapping("/{consultaId}")
    public ConsultaResumoResponse buscar(@PathVariable Long cidadaoId, @PathVariable Long consultaId) {
        return service.buscar(cidadaoId, consultaId);
    }

    @PutMapping("/{consultaId}")
    public ConsultaRespostaResponse atualizar(@PathVariable Long cidadaoId, @PathVariable Long consultaId,
                                              @Valid @RequestBody ConsultaRequest req) {
        return service.atualizar(cidadaoId, consultaId, req);
    }

    @DeleteMapping("/{consultaId}")
    public ResponseEntity<Void> remover(@PathVariable Long cidadaoId, @PathVariable Long consultaId) {
        service.remover(cidadaoId, consultaId);
        return ResponseEntity.noContent().build();
    }
}
