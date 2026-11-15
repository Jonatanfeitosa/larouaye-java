package br.com.fiap.larouaye.dto;

import br.com.fiap.larouaye.model.StatusEtapa;
import java.time.LocalDateTime;

/** Saída: progresso em uma etapa. */
public class ProgressoResponse {

    private Long etapaId;

    private StatusEtapa status;

    private LocalDateTime atualizadoEm;

    public ProgressoResponse() {
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

    public LocalDateTime getAtualizadoEm() {
        return atualizadoEm;
    }

    public void setAtualizadoEm(LocalDateTime atualizadoEm) {
        this.atualizadoEm = atualizadoEm;
    }
}
