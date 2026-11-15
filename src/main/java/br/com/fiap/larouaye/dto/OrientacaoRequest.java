package br.com.fiap.larouaye.dto;

import br.com.fiap.larouaye.model.StatusRevisao;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

/** Entrada: orientação jurídica. */
public class OrientacaoRequest {

    @NotNull(message = "Informe a categoria.")
    @Positive(message = "A categoria deve ser um número positivo.")
    private Long categoriaId;

    @NotBlank(message = "Informe o título.")
    @Size(min = 3, max = 150, message = "O título deve ter de 3 a 150 letras.")
    private String titulo;

    @NotBlank(message = "Informe a pergunta relacionada.")
    @Size(min = 3, max = 200, message = "A pergunta deve ter de 3 a 200 letras.")
    private String perguntaRelacionada;

    @NotBlank(message = "Informe o conteúdo.")
    @Size(min = 10, max = 3000, message = "O conteúdo deve ter de 10 a 3000 letras.")
    private String conteudo;

    @Pattern(regexp = "^$|^https?://\\S+$", message = "Informe um link que comece com http ou https.")
    @Size(min = 0, max = 300, message = "O link deve ter até 300 letras.")
    private String urlVideo;

    /** Padrão PENDENTE. */
    private StatusRevisao revisao;

    /** Não pode ser futura. */
    @PastOrPresent(message = "A data de revisão não pode ser futura.")
    private LocalDate dataRevisao;

    /** Padrão true. */
    private Boolean ativo;

    @Size(min = 0, max = 15, message = "Use no máximo 15 cuidados.")
    private List<String> cuidados;

    @Valid
    @Size(min = 0, max = 15, message = "Use no máximo 15 fontes.")
    private List<FonteLegalDto> baseLegal;

    public OrientacaoRequest() {
    }

    public Long getCategoriaId() {
        return categoriaId;
    }

    public void setCategoriaId(Long categoriaId) {
        this.categoriaId = categoriaId;
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

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
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
