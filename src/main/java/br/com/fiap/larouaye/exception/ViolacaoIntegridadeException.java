package br.com.fiap.larouaye.exception;

/** O banco recusou por chave única ou por chave estrangeira (ORA-00001, ORA-02292...). Vira HTTP 409. */
public class ViolacaoIntegridadeException extends RuntimeException {

    public ViolacaoIntegridadeException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
