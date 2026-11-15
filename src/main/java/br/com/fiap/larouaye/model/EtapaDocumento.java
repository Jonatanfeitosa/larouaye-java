package br.com.fiap.larouaye.model;

/** Documento necessário em uma etapa. Tabela tb_etapa_documento (filha de etapa). */
public class EtapaDocumento {

    /** cd_documento */
    private Long id;

    /** nm_documento */
    private String nome;

    /** ds_observacao */
    private String observacao;

    public EtapaDocumento() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }
}
