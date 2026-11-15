package br.com.fiap.larouaye.exception;

/** Uma regra do domínio foi violada (ex.: e-mail já cadastrado, remover categoria com orientações). Vira HTTP 409. */
public class RegraDeNegocioException extends RuntimeException {

    public RegraDeNegocioException(String mensagem) {
        super(mensagem);
    }
}
