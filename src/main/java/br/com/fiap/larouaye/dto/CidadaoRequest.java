package br.com.fiap.larouaye.dto;

import br.com.fiap.larouaye.model.Canal;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Entrada: cadastro ou atualização de cidadão. */
public class CidadaoRequest {

    @NotBlank(message = "Informe seu nome.")
    @Size(min = 2, max = 80, message = "O nome deve ter de 2 a 80 letras.")
    private String nome;

    @Email(message = "Informe um e-mail válido.")
    @Size(min = 0, max = 120, message = "O e-mail deve ter até 120 letras.")
    private String email;

    /** Padrão WEB quando não informado. */
    private Canal canal;

    /** Obrigatório quando o canal é TELEGRAM. */
    @Size(min = 0, max = 40, message = "O id externo deve ter até 40 letras.")
    private String idExterno;

    public CidadaoRequest() {
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Canal getCanal() {
        return canal;
    }

    public void setCanal(Canal canal) {
        this.canal = canal;
    }

    public String getIdExterno() {
        return idExterno;
    }

    public void setIdExterno(String idExterno) {
        this.idExterno = idExterno;
    }
}
