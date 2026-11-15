package br.com.fiap.larouaye.model;

import java.time.LocalDateTime;

/** Pessoa atendida pelo LarOuAyê (web ou Telegram). Tabela tb_cidadao. Só dados mínimos, sem CPF. */
public class Cidadao {

    /** cd_cidadao */
    private Long id;

    /** nm_cidadao */
    private String nome;

    /** ds_email (opcional, único) */
    private String email;

    /** ds_canal */
    private Canal canal;

    /** cd_id_externo (chat_id do Telegram) */
    private String idExterno;

    /** dt_cadastro */
    private LocalDateTime dataCadastro;

    public Cidadao() {
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

    public String getIdExterno() {
        return idExterno;
    }

    public void setIdExterno(String idExterno) {
        this.idExterno = idExterno;
    }

    public LocalDateTime getDataCadastro() {
        return dataCadastro;
    }

    public void setDataCadastro(LocalDateTime dataCadastro) {
        this.dataCadastro = dataCadastro;
    }
}
