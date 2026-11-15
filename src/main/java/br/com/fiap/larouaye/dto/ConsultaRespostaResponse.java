package br.com.fiap.larouaye.dto;

import br.com.fiap.larouaye.model.StatusConsulta;
import java.util.List;

/** Saída: resultado de uma consulta (contrato com o front). */
public class ConsultaRespostaResponse {

    private Long consultaId;

    private StatusConsulta status;

    private String categoria;

    private RespostaConteudo resposta;

    private String proximaEtapa;

    private List<String> opcoes;

    /** Texto simples quando não há resposta. */
    private String mensagem;

    public ConsultaRespostaResponse() {
    }

    public Long getConsultaId() {
        return consultaId;
    }

    public void setConsultaId(Long consultaId) {
        this.consultaId = consultaId;
    }

    public StatusConsulta getStatus() {
        return status;
    }

    public void setStatus(StatusConsulta status) {
        this.status = status;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public RespostaConteudo getResposta() {
        return resposta;
    }

    public void setResposta(RespostaConteudo resposta) {
        this.resposta = resposta;
    }

    public String getProximaEtapa() {
        return proximaEtapa;
    }

    public void setProximaEtapa(String proximaEtapa) {
        this.proximaEtapa = proximaEtapa;
    }

    public List<String> getOpcoes() {
        return opcoes;
    }

    public void setOpcoes(List<String> opcoes) {
        this.opcoes = opcoes;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }
}
