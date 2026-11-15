package br.com.fiap.larouaye.dto;

import br.com.fiap.larouaye.model.StatusConsulta;
import java.time.LocalDateTime;

/** Saída: item do histórico. */
public class ConsultaResumoResponse {

    private Long consultaId;

    private String pergunta;

    private String uf;

    private String categoria;

    private StatusConsulta status;

    private LocalDateTime dataConsulta;

    public ConsultaResumoResponse() {
    }

    public Long getConsultaId() {
        return consultaId;
    }

    public void setConsultaId(Long consultaId) {
        this.consultaId = consultaId;
    }

    public String getPergunta() {
        return pergunta;
    }

    public void setPergunta(String pergunta) {
        this.pergunta = pergunta;
    }

    public String getUf() {
        return uf;
    }

    public void setUf(String uf) {
        this.uf = uf;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public StatusConsulta getStatus() {
        return status;
    }

    public void setStatus(StatusConsulta status) {
        this.status = status;
    }

    public LocalDateTime getDataConsulta() {
        return dataConsulta;
    }

    public void setDataConsulta(LocalDateTime dataConsulta) {
        this.dataConsulta = dataConsulta;
    }
}
