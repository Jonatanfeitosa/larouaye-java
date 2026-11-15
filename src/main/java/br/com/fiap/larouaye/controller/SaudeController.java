package br.com.fiap.larouaye.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.LinkedHashMap;
import java.util.Map;

/** Verificação de saúde (usada pelo Render e pelo front): 200 se o banco responde, 503 se não. */
@RestController
@RequestMapping("/api/saude")
public class SaudeController {

    private final DataSource dataSource;

    public SaudeController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping
    public ResponseEntity<Map<String, String>> saude() {
        Map<String, String> corpo = new LinkedHashMap<>();
        corpo.put("servico", "larouaye-api");
        boolean bancoOk;
        try (Connection con = dataSource.getConnection()) {
            bancoOk = con.isValid(2);
        } catch (Exception e) {
            bancoOk = false;
        }
        corpo.put("banco", bancoOk ? "OK" : "FORA");
        corpo.put("status", bancoOk ? "OK" : "DEGRADADO");
        return ResponseEntity.status(bancoOk ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE).body(corpo);
    }
}
