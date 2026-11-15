package br.com.fiap.larouaye.dto;

import br.com.fiap.larouaye.model.Canal;
import java.time.LocalDateTime;

/** Saída: cidadão. */
public class CidadaoResponse {

    private Long id;

    private String nome;

    private String email;

    private Canal canal;

    private LocalDateTime dataCadastro;

    public CidadaoResponse() {
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

    public LocalDateTime getDataCadastro() {
        return dataCadastro;
    }

    public void setDataCadastro(LocalDateTime dataCadastro) {
        this.dataCadastro = dataCadastro;
    }
}
