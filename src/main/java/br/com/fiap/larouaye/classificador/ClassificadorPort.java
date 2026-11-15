package br.com.fiap.larouaye.classificador;

/** Porta de saída: "dada uma pergunta, qual a categoria?". O Java não depende de como isso é feito. */
public interface ClassificadorPort {

    /** Nunca devolve nulo. Lança {@link ClassificadorIndisponivelException} se não conseguir classificar. */
    ResultadoClassificacao classificar(String pergunta);
}
