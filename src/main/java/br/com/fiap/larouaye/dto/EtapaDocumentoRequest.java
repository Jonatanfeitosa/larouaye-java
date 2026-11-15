package br.com.fiap.larouaye.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Entrada: documento de uma etapa. */
public class EtapaDocumentoRequest {

    @NotBlank(message = "Informe o nome do documento.")
    @Size(min = 2, max = 120, message = "O nome deve ter de 2 a 120 letras.")
    private String nome;

    @Size(min = 0, max = 300, message = "A observação deve ter até 300 letras.")
    private String observacao;

    public EtapaDocumentoRequest() {
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
