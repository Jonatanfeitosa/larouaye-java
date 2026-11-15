package br.com.fiap.larouaye.exception;

/** O registro pedido não existe. Vira HTTP 404. */
public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String recurso, Object id) {
        super(recurso + " não encontrado(a): " + id + ".");
    }

    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
