package br.com.fiap.larouaye.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Entrada: categoria. */
public class CategoriaRequest {

    /** Letras maiúsculas e sublinhado. */
    @NotBlank(message = "Informe o código.")
    @Pattern(regexp = "[A-Z_]{3,30}", message = "Use de 3 a 30 letras maiúsculas ou sublinhado.")
    private String codigo;

    @NotBlank(message = "Informe o nome.")
    @Size(min = 2, max = 80, message = "O nome deve ter de 2 a 80 letras.")
    private String nome;

    @Size(min = 0, max = 300, message = "A descrição deve ter até 300 letras.")
    private String descricao;

    public CategoriaRequest() {
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
