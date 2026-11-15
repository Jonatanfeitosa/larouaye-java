package br.com.fiap.larouaye.model;

/** Assunto jurídico das dúvidas. Tabela tb_categoria (lado "1" do 1:N com orientação). */
public class Categoria {

    /** cd_categoria */
    private Long id;

    /** cd_codigo, único (ex.: CONTRATO) */
    private String codigo;

    /** nm_categoria */
    private String nome;

    /** ds_categoria */
    private String descricao;

    public Categoria() {
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
}
