package br.com.fiap.larouaye.service;

import java.util.List;
import java.util.Map;

/**
 * Camada anticorrupção entre o vocabulário da IA e o do banco.
 * O modelo usa 10 rótulos; o banco tem 6 categorias jurídicas. Quando não há orientação revisada
 * para um rótulo (ex.: LEILAO), a lista é vazia e o sistema NÃO inventa resposta: encaminha.
 */
final class MapaCategorias {

    static final String FORA_DO_ESCOPO = "FORA_DO_ESCOPO";

    private static final Map<String, List<String>> MAPA = Map.of(
            "MATRICULA_ONUS", List.of("DOCUMENTACAO"),
            "ESCRITURA_REGISTRO", List.of("ESCRITURA", "REGISTRO"),
            "CONTRATO", List.of("CONTRATO"),
            "FINANCIAMENTO", List.of("FINANCIAMENTO"),
            "FGTS_SUBSIDIOS", List.of("FINANCIAMENTO"),
            "IMPOSTOS_TAXAS", List.of("TRIBUTOS"),
            "IMOVEL_USADO", List.of("DOCUMENTACAO"),
            "LEILAO", List.of(),
            "FRAUDE_GOLPES", List.of());

    private MapaCategorias() {
    }

    /** Códigos de categoria do banco que atendem ao rótulo do modelo. Rótulo desconhecido atende ao próprio código. */
    static List<String> paraBanco(String rotuloModelo) {
        if (rotuloModelo == null || FORA_DO_ESCOPO.equals(rotuloModelo)) {
            return List.of();
        }
        return MAPA.getOrDefault(rotuloModelo, List.of(rotuloModelo));
    }
}
