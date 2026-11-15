package br.com.fiap.larouaye.dto;

import br.com.fiap.larouaye.model.Canal;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Entrada: pergunta do cidadão. */
public class ConsultaRequest {

    @NotBlank(message = "Escreva sua dúvida.")
    @Size(min = 3, max = 500, message = "Escreva sua dúvida com 3 a 500 letras.")
    private String pergunta;

    @NotBlank(message = "Informe a UF.")
    @Pattern(regexp = "[A-Za-z]{2}", message = "Use a sigla da UF com 2 letras, por exemplo SP.")
    private String uf;

    /** Código da etapa em que a pessoa está (opcional). */
    @Size(min = 0, max = 30, message = "A etapa deve ter até 30 letras.")
    private String etapa;

    /** Escolha feita após um pedido de esclarecimento (opcional): código ou nome. */
    @Size(min = 0, max = 80, message = "A categoria deve ter até 80 letras.")
    private String categoria;

    /** Padrão WEB. */
    private Canal canal;

    public ConsultaRequest() {
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

    public String getEtapa() {
        return etapa;
    }

    public void setEtapa(String etapa) {
        this.etapa = etapa;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public Canal getCanal() {
        return canal;
    }

    public void setCanal(Canal canal) {
        this.canal = canal;
    }
}
