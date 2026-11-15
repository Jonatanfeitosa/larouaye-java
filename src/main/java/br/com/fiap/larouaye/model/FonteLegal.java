package br.com.fiap.larouaye.model;

/** Lei ou fonte oficial que fundamenta uma orientação. Tabela tb_fonte_legal (filha de orientação). */
public class FonteLegal {

    /** cd_fonte */
    private Long id;

    /** nm_norma */
    private String norma;

    /** ds_referencia */
    private String referencia;

    /** ds_url */
    private String url;

    public FonteLegal() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNorma() {
        return norma;
    }

    public void setNorma(String norma) {
        this.norma = norma;
    }

    public String getReferencia() {
        return referencia;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
