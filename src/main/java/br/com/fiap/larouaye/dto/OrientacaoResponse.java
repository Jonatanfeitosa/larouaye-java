package br.com.fiap.larouaye.dto;

import br.com.fiap.larouaye.model.StatusRevisao;
import java.time.LocalDate;
import java.util.List;

/** Saída: orientação. O campo "categoria" é o CÓDIGO da categoria (contrato do front). */
public class OrientacaoResponse {

    private Long id;

    private Long categoriaId;

    private String categoria;

    private String titulo;

    private String perguntaRelacionada;

    private String conteudo;

    private String urlVideo;

    private StatusRevisao revisao;

    /** Só aparece quando REVISADO. */
    private LocalDate dataRevisao;

    private boolean ativo;

    private List<String> cuidados;

    private List<FonteLegalDto> baseLegal;

    public OrientacaoResponse() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCategoriaId() {
        return categoriaId;
    }

    public void setCategoriaId(Long categoriaId) {
        this.categoriaId = categoriaId;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getPerguntaRelacionada() {
        return perguntaRelacionada;
    }

    public void setPerguntaRelacionada(String perguntaRelacionada) {
        this.perguntaRelacionada = perguntaRelacionada;
    }

    public String getConteudo() {
        return conteudo;
    }

    public void setConteudo(String conteudo) {
        this.conteudo = conteudo;
    }

    public String getUrlVideo() {
        return urlVideo;
    }

    public void setUrlVideo(String urlVideo) {
        this.urlVideo = urlVideo;
    }

    public StatusRevisao getRevisao() {
        return revisao;
    }

    public void setRevisao(StatusRevisao revisao) {
        this.revisao = revisao;
    }

    public LocalDate getDataRevisao() {
        return dataRevisao;
    }

    public void setDataRevisao(LocalDate dataRevisao) {
        this.dataRevisao = dataRevisao;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
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
}
