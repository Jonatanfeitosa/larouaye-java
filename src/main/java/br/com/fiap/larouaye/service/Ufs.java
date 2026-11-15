package br.com.fiap.larouaye.service;

import java.util.Locale;
import java.util.Set;

/** As 27 unidades da federação. */
final class Ufs {

    private static final Set<String> VALIDAS = Set.of("AC", "AL", "AP", "AM", "BA", "CE", "DF", "ES", "GO", "MA", "MT", "MS", "MG",
            "PA", "PB", "PR", "PE", "PI", "RJ", "RN", "RS", "RO", "RR", "SC", "SP", "SE", "TO");

    private Ufs() {
    }

    static String normalizar(String uf) {
        return uf == null ? "" : uf.trim().toUpperCase(Locale.ROOT);
    }

    static boolean valida(String uf) {
        return VALIDAS.contains(uf);
    }
}
