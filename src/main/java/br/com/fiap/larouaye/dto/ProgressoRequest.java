package br.com.fiap.larouaye.dto;

import br.com.fiap.larouaye.model.StatusEtapa;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/** Entrada: marcar o progresso em uma etapa. */
public class ProgressoRequest {

    @NotNull(message = "Informe a etapa.")
    @Positive(message = "A etapa deve ser um número positivo.")
    private Long etapaId;

    @NotNull(message = "Informe o status.")
    private StatusEtapa status;

    public ProgressoRequest() {
    }

    public Long getEtapaId() {
        return etapaId;
    }

    public void setEtapaId(Long etapaId) {
        this.etapaId = etapaId;
    }

    public StatusEtapa getStatus() {
        return status;
    }

    public void setStatus(StatusEtapa status) {
        this.status = status;
    }
}
