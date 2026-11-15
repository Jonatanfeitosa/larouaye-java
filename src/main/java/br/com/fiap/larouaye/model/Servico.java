package br.com.fiap.larouaye.model;

/** Canal oficial (cartório, prefeitura, banco, defensoria) por UF. Tabela tb_servico. */
public class Servico {

    /** cd_servico */
    private Long id;

    /** nm_servico */
    private String nome;

    /** ds_tipo */
    private TipoServico tipo;

    /** sg_uf */
    private String uf;

    /** ds_url */
    private String url;

    /** ds_descricao */
    private String descricao;

    public Servico() {
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

    public TipoServico getTipo() {
        return tipo;
    }

    public void setTipo(TipoServico tipo) {
        this.tipo = tipo;
    }

    public String getUf() {
        return uf;
    }

    public void setUf(String uf) {
        this.uf = uf;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
}
