package br.com.fiap.larouaye.model;

import java.util.List;

/** RAIZ DO AGREGADO de etapa da compra, com seus documentos. Tabela tb_etapa. */
public class Etapa {

    /** cd_etapa */
    private Long id;

    /** cd_codigo, único (ex.: ESCRITURA) */
    private String codigo;

    /** nr_ordem, único */
    private int ordem;

    /** nm_etapa */
    private String nome;

    /** ds_etapa */
    private String descricao;

    /** tb_etapa_documento */
    private List<EtapaDocumento> documentos;

    public Etapa() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public int getOrdem() {
        return ordem;
    }

    public void setOrdem(int ordem) {
        this.ordem = ordem;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public List<EtapaDocumento> getDocumentos() {
        return documentos;
    }

    public void setDocumentos(List<EtapaDocumento> documentos) {
        this.documentos = documentos;
    }
}
