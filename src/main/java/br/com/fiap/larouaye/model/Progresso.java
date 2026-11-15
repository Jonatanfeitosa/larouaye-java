package br.com.fiap.larouaye.model;

import java.time.LocalDateTime;

/** Progresso de um cidadão em uma etapa (N:N). Tabela tb_cidadao_etapa. */
public class Progresso {

    /** cd_cidadao */
    private Long cidadaoId;

    /** cd_etapa */
    private Long etapaId;

    /** ds_status */
    private StatusEtapa status;

    /** dt_atualizacao */
    private LocalDateTime atualizadoEm;

    public Progresso() {
    }

    public Long getCidadaoId() {
        return cidadaoId;
    }

    public void setCidadaoId(Long cidadaoId) {
        this.cidadaoId = cidadaoId;
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
