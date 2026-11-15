package br.com.fiap.larouaye.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Entrada e saída: fonte legal de uma orientação. */
public class FonteLegalDto {

    @NotBlank(message = "Informe a norma.")
    @Size(min = 1, max = 120, message = "A norma deve ter até 120 letras.")
    private String norma;

    @Size(min = 0, max = 200, message = "A referência deve ter até 200 letras.")
    private String referencia;

    @Pattern(regexp = "^$|^https?://\\S+$", message = "Informe um link que comece com http ou https.")
    @Size(min = 0, max = 300, message = "O link deve ter até 300 letras.")
    private String url;

    public FonteLegalDto() {
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
