package br.com.fiap.larouaye.model;

import java.time.LocalDate;
import java.util.List;

/** RAIZ DO AGREGADO de conteúdo jurídico: carrega seus cuidados e fontes. Tabela tb_orientacao. */
public class Orientacao {

    /** cd_orientacao */
    private Long id;

    /** cd_categoria (FK) */
    private Long categoriaId;

    /** código da categoria (só leitura, vem do JOIN) */
    private String categoriaCodigo;

    /** nm_titulo */
    private String titulo;

    /** ds_pergunta */
    private String perguntaRelacionada;

    /** ds_conteudo, em linguagem simples */
    private String conteudo;

    /** ds_url_video (opcional) */
    private String urlVideo;

    /** PENDENTE ou REVISADO */
    private StatusRevisao revisao;

    /** dt_revisao; obrigatória quando REVISADO */
    private LocalDate dataRevisao;

    /** st_ativo */
    private boolean ativo;

    /** tb_cuidado, em ordem */
    private List<String> cuidados;

    /** tb_fonte_legal */
    private List<FonteLegal> baseLegal;

    public Orientacao() {
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

    public String getCategoriaCodigo() {
        return categoriaCodigo;
    }

    public void setCategoriaCodigo(String categoriaCodigo) {
        this.categoriaCodigo = categoriaCodigo;
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

    public List<FonteLegal> getBaseLegal() {
        return baseLegal;
    }

    public void setBaseLegal(List<FonteLegal> baseLegal) {
        this.baseLegal = baseLegal;
    }
}
