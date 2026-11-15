package br.com.fiap.larouaye.dto;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Formato único de erro da API. O front lê "mensagem"; "campos" traz o erro de cada campo (Bean Validation).
 * Nunca contém stack trace nem detalhes de SQL.
 */
public class ErroResponse {

    private final LocalDateTime timestamp = LocalDateTime.now();
    private final int status;
    private final String erro;
    private final String mensagem;
    private final String caminho;
    private final Map<String, String> campos;

    public ErroResponse(int status, String erro, String mensagem, String caminho, Map<String, String> campos) {
        this.status = status;
        this.erro = erro;
        this.mensagem = mensagem;
        this.caminho = caminho;
        this.campos = campos;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public String getErro() {
        return erro;
    }

    public String getMensagem() {
        return mensagem;
    }

    public String getCaminho() {
        return caminho;
    }

    public Map<String, String> getCampos() {
        return campos;
    }
}
