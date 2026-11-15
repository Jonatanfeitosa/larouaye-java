package br.com.fiap.larouaye.classificador;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Chama o serviço Flask (POST /api/classificar) com tempo limite. Qualquer falha vira {@link ClassificadorIndisponivelException}. */
@Component
public class ClassificadorFlask {

    private final RestClient cliente;

    public ClassificadorFlask(@Qualifier("classificadorRestClient") RestClient cliente) {
        this.cliente = cliente;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Candidata(String categoria, Double confianca) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Resposta(String categoria, Double confianca, List<Candidata> maisProvaveis, String etapaSugerida) {
    }

    public ResultadoClassificacao classificar(String pergunta) {
        try {
            Resposta r = cliente.post().uri("/api/classificar").body(Map.of("pergunta", pergunta)).retrieve().body(Resposta.class);
            if (r == null || r.categoria() == null || r.confianca() == null) {
                throw new ClassificadorIndisponivelException("Resposta vazia do classificador.", null);
            }
            List<ResultadoClassificacao.Candidata> candidatas = new ArrayList<>();
            if (r.maisProvaveis() != null) {
                for (Candidata c : r.maisProvaveis()) {
                    if (c.categoria() != null && c.confianca() != null) {
                        candidatas.add(new ResultadoClassificacao.Candidata(c.categoria(), c.confianca()));
                    }
                }
            }
            return new ResultadoClassificacao(r.categoria(), r.confianca(), candidatas, r.etapaSugerida(), "FLASK");
        } catch (RestClientException e) {
            throw new ClassificadorIndisponivelException("Classificador fora do ar ou lento.", e);
        }
    }
}
