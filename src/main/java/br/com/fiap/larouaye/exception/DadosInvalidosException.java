package br.com.fiap.larouaye.exception;

/** Dado inválido que o Bean Validation não consegue checar sozinho (regra entre campos). Vira HTTP 400. */
public class DadosInvalidosException extends RuntimeException {

    public DadosInvalidosException(String mensagem) {
        super(mensagem);
    }
}
