package br.com.fiap.larouaye.classificador;

/** O serviço de IA não respondeu a tempo ou respondeu algo inválido. */
public class ClassificadorIndisponivelException extends RuntimeException {

    public ClassificadorIndisponivelException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
