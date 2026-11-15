package br.com.fiap.larouaye.exception;

/**
 * Falha inesperada ao falar com o banco. A mensagem é genérica de propósito: o SQL e o erro do Oracle
 * ficam só no log do servidor (na causa), nunca na resposta HTTP. Vira HTTP 500.
 */
public class PersistenciaException extends RuntimeException {

    public PersistenciaException(String operacao, Throwable causa) {
        super("Falha ao acessar o banco de dados (" + operacao + ").", causa);
    }
}
