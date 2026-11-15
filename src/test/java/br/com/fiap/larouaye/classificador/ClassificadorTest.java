package br.com.fiap.larouaye.classificador;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClassificadorTest {

    private final ClassificadorPorRegras regras = new ClassificadorPorRegras();

    @Test
    void regrasReconhecemMatriculaIgnorandoAcento() {
        ResultadoClassificacao r = regras.classificar("Como pedir a certidão da matrícula e ver se há ônus?");
        assertEquals("MATRICULA_ONUS", r.categoria());
        assertEquals("REGRAS", r.origem());
    }

    @Test
    void regrasMandamPerguntaSemPalavrasConhecidasParaForaDoEscopo() {
        ResultadoClassificacao r = regras.classificar("qual a previsão do tempo amanhã?");
        assertEquals("FORA_DO_ESCOPO", r.categoria());
        assertEquals(0.0, r.confianca());
    }

    @Test
    void confiancaDoPlanoBNuncaPassaDoTeto() {
        ResultadoClassificacao r = regras.classificar("itbi");
        assertTrue(r.confianca() <= 0.85);
    }

    @Test
    void quandoOFlaskCaiOPlanoBAssume() {
        ClassificadorFlask flaskForaDoAr = new ClassificadorFlask(null) {
            @Override
            public ResultadoClassificacao classificar(String pergunta) {
                throw new ClassificadorIndisponivelException("fora do ar", null);
            }
        };
        ClassificadorPort porta = new ClassificadorComFallback(flaskForaDoAr, regras);
        assertEquals("IMPOSTOS_TAXAS", porta.classificar("Quem paga o ITBI?").categoria());
    }

    @Test
    void quandoOFlaskRespondeUsaARespostaDele() {
        ClassificadorFlask flask = new ClassificadorFlask(null) {
            @Override
            public ResultadoClassificacao classificar(String pergunta) {
                return new ResultadoClassificacao("CONTRATO", 0.93, java.util.List.of(), null, "FLASK");
            }
        };
        ResultadoClassificacao r = new ClassificadorComFallback(flask, regras).classificar("qualquer coisa");
        assertEquals("FLASK", r.origem());
    }
}
