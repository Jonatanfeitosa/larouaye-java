package br.com.fiap.larouaye.controller;

import br.com.fiap.larouaye.dto.ServicoResponse;
import br.com.fiap.larouaye.service.ServicoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Guia de serviços: canais oficiais por UF, ex.: GET /api/servicos?uf=SP. */
@RestController
@RequestMapping("/api/servicos")
public class ServicoController {

    private final ServicoService service;

    public ServicoController(ServicoService service) {
        this.service = service;
    }

    @GetMapping
    public List<ServicoResponse> listar(@RequestParam String uf) {
        return service.listarPorUf(uf);
    }
}
