package br.com.fiap.larouaye.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Uma pergunta do cidadão, a classificação feita pela IA e o que foi entregue. Tabela tb_consulta (lado "N" do 1:N com cidadão). */
public class Consulta {

    /** cd_consulta */
    private Long id;

    /** cd_cidadao (FK) */
    private Long cidadaoId;

    /** cd_categoria (FK, pode ser nulo) */
    private Long categoriaId;

    /** cd_orientacao (FK, pode ser nulo) */
    private Long orientacaoId;

    /** código da categoria (só leitura, vem do JOIN) */
    private String categoriaCodigo;

    /** ds_pergunta */
    private String pergunta;

    /** sg_uf em maiúsculas */
    private String uf;

    /** ds_etapa_atual (código da etapa) */
    private String etapaAtual;

    /** vl_confianca de 0 a 1, informativa */
    private BigDecimal confianca;

    /** ds_status */
    private StatusConsulta status;

    /** ds_canal */
    private Canal canal;

    /** dt_consulta */
    private LocalDateTime dataConsulta;

    public Consulta() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCidadaoId() {
        return cidadaoId;
    }

    public void setCidadaoId(Long cidadaoId) {
        this.cidadaoId = cidadaoId;
    }

    public Long getCategoriaId() {
        return categoriaId;
    }

    public void setCategoriaId(Long categoriaId) {
        this.categoriaId = categoriaId;
    }

    public Long getOrientacaoId() {
        return orientacaoId;
    }

    public void setOrientacaoId(Long orientacaoId) {
        this.orientacaoId = orientacaoId;
    }

    public String getCategoriaCodigo() {
        return categoriaCodigo;
    }

    public void setCategoriaCodigo(String categoriaCodigo) {
        this.categoriaCodigo = categoriaCodigo;
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

    public String getEtapaAtual() {
        return etapaAtual;
    }

    public void setEtapaAtual(String etapaAtual) {
        this.etapaAtual = etapaAtual;
    }

    public BigDecimal getConfianca() {
        return confianca;
    }

    public void setConfianca(BigDecimal confianca) {
        this.confianca = confianca;
    }

    public StatusConsulta getStatus() {
        return status;
    }

    public void setStatus(StatusConsulta status) {
        this.status = status;
    }

    public Canal getCanal() {
        return canal;
    }

    public void setCanal(Canal canal) {
        this.canal = canal;
    }

    public LocalDateTime getDataConsulta() {
        return dataConsulta;
    }

    public void setDataConsulta(LocalDateTime dataConsulta) {
        this.dataConsulta = dataConsulta;
    }
}
