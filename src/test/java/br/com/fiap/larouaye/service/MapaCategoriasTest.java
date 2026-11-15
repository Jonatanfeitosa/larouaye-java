package br.com.fiap.larouaye.service;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MapaCategoriasTest {

    @Test
    void rotulosDoModeloViramCategoriasDoBanco() {
        assertEquals(List.of("DOCUMENTACAO"), MapaCategorias.paraBanco("MATRICULA_ONUS"));
        assertEquals(List.of("ESCRITURA", "REGISTRO"), MapaCategorias.paraBanco("ESCRITURA_REGISTRO"));
        assertEquals(List.of("FINANCIAMENTO"), MapaCategorias.paraBanco("FGTS_SUBSIDIOS"));
    }

    @Test
    void rotulosSemOrientacaoRevisadaFicamVazios() {
        assertTrue(MapaCategorias.paraBanco("LEILAO").isEmpty());
        assertTrue(MapaCategorias.paraBanco("FRAUDE_GOLPES").isEmpty());
        assertTrue(MapaCategorias.paraBanco("FORA_DO_ESCOPO").isEmpty());
        assertTrue(MapaCategorias.paraBanco(null).isEmpty());
    }
}
