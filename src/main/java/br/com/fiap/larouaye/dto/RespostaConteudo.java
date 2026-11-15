package br.com.fiap.larouaye.dto;

import java.time.LocalDate;
import java.util.List;

/** Parte "resposta" da resposta de uma consulta. */
public class RespostaConteudo {

    private String titulo;

    private String conteudo;

    private List<String> cuidados;

    private List<FonteLegalDto> baseLegal;

    private LocalDate dataRevisao;

    private Long orientacaoId;

    public RespostaConteudo() {
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getConteudo() {
        return conteudo;
    }

    public void setConteudo(String conteudo) {
        this.conteudo = conteudo;
    }

    public List<String> getCuidados() {
        return cuidados;
    }

    public void setCuidados(List<String> cuidados) {
        this.cuidados = cuidados;
    }

    public List<FonteLegalDto> getBaseLegal() {
        return baseLegal;
    }

    public void setBaseLegal(List<FonteLegalDto> baseLegal) {
        this.baseLegal = baseLegal;
    }

    public LocalDate getDataRevisao() {
        return dataRevisao;
    }

    public void setDataRevisao(LocalDate dataRevisao) {
        this.dataRevisao = dataRevisao;
    }

    public Long getOrientacaoId() {
        return orientacaoId;
    }

    public void setOrientacaoId(Long orientacaoId) {
        this.orientacaoId = orientacaoId;
    }
}
