package br.com.fiap.larouaye.service;

import br.com.fiap.larouaye.dto.ConsultaRequest;
import br.com.fiap.larouaye.dto.ConsultaRespostaResponse;
import br.com.fiap.larouaye.exception.DadosInvalidosException;
import br.com.fiap.larouaye.exception.RecursoNaoEncontradoException;
import br.com.fiap.larouaye.model.Cidadao;
import br.com.fiap.larouaye.model.StatusConsulta;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Testa a REGRA DE DECISÃO: a IA sugere, o Java decide. */
class ConsultaServiceTest {

    private ConsultaRequest pedido(String pergunta) {
        ConsultaRequest r = new ConsultaRequest();
        r.setPergunta(pergunta);
        r.setUf("sp");
        r.setEtapa("ANALISE_DOCUMENTAL");
        return r;
    }

    @Test
    void confiancaAltaComOrientacaoNaCategoriaRespondeEGravaHistorico() {
        Cenario c = new Cenario();
        c.classificadorDiz("MATRICULA_ONUS", 0.91);
        ConsultaRespostaResponse r = c.consultaService.perguntar(c.maria.getId(), pedido("Como descubro se um imóvel tem dívidas?"));
        assertEquals(StatusConsulta.RESPONDIDA, r.getStatus());
        assertEquals("DOCUMENTACAO", r.getCategoria());
        assertNotNull(r.getResposta());
        // escolhe a orientação mais parecida com a pergunta, não só a primeira da categoria
        assertEquals(c.orientDivida.getId(), r.getResposta().getOrientacaoId());
        assertEquals("Negociação e contrato", r.getProximaEtapa());
        assertEquals(1, c.consultaService.listar(c.maria.getId()).size());
        assertEquals("SP", c.banco.consultas.get(r.getConsultaId()).getUf());
    }

    @Test
    void confiancaIntermediariaPedeEsclarecimentoSemChutarResposta() {
        Cenario c = new Cenario();
        c.classificadorDiz("MATRICULA_ONUS", 0.45, "IMPOSTOS_TAXAS");
        ConsultaRespostaResponse r = c.consultaService.perguntar(c.maria.getId(), pedido("preciso de ajuda com o imóvel"));
        assertEquals(StatusConsulta.ESCLARECIMENTO, r.getStatus());
        assertNull(r.getResposta());
        assertEquals(2, r.getOpcoes().size());
        assertTrue(r.getOpcoes().contains("Documentação"));
        assertEquals(StatusConsulta.ESCLARECIMENTO, c.banco.consultas.get(r.getConsultaId()).getStatus());
    }

    @Test
    void confiancaBaixaENaoReconhecida() {
        Cenario c = new Cenario();
        c.classificadorDiz("CONTRATO", 0.10);
        ConsultaRespostaResponse r = c.consultaService.perguntar(c.maria.getId(), pedido("asdf qwer"));
        assertEquals(StatusConsulta.NAO_RECONHECIDA, r.getStatus());
        assertNull(r.getResposta());
        assertNotNull(r.getMensagem());
    }

    @Test
    void foraDoEscopoNuncaRespondeMesmoComConfiancaAlta() {
        Cenario c = new Cenario();
        c.classificadorDiz("FORA_DO_ESCOPO", 0.99);
        ConsultaRespostaResponse r = c.consultaService.perguntar(c.maria.getId(), pedido("quero financiar um carro"));
        assertEquals(StatusConsulta.NAO_RECONHECIDA, r.getStatus());
        assertNull(r.getResposta());
    }

    @Test
    void categoriaSemOrientacaoRevisadaNaoInventaResposta() {
        Cenario c = new Cenario();
        c.classificadorDiz("LEILAO", 0.95); // o banco não tem categoria equivalente
        ConsultaRespostaResponse r = c.consultaService.perguntar(c.maria.getId(), pedido("como funciona leilão de imóvel?"));
        assertEquals(StatusConsulta.NAO_RECONHECIDA, r.getStatus());
        assertNull(r.getResposta());
    }

    @Test
    void orientacaoInativaNaoEEntregue() {
        Cenario c = new Cenario();
        c.orientItbi.setAtivo(false);
        c.classificadorDiz("IMPOSTOS_TAXAS", 0.95);
        ConsultaRespostaResponse r = c.consultaService.perguntar(c.maria.getId(), pedido("O que é o ITBI?"));
        assertEquals(StatusConsulta.NAO_RECONHECIDA, r.getStatus());
    }

    @Test
    void categoriaEscolhidaPelaPessoaDispensaOClassificador() {
        Cenario c = new Cenario();
        c.proximoResultado = null; // se o classificador fosse chamado, daria NullPointerException
        ConsultaRequest req = pedido("e agora?");
        req.setCategoria("Impostos e taxas");
        ConsultaRespostaResponse r = c.consultaService.perguntar(c.maria.getId(), req);
        assertEquals(StatusConsulta.RESPONDIDA, r.getStatus());
        assertEquals("TRIBUTOS", r.getCategoria());
    }

    @Test
    void ufInvalidaEhRejeitada() {
        Cenario c = new Cenario();
        c.classificadorDiz("CONTRATO", 0.9);
        ConsultaRequest req = pedido("Pergunta válida");
        req.setUf("ZZ");
        assertThrows(DadosInvalidosException.class, () -> c.consultaService.perguntar(c.maria.getId(), req));
        assertEquals(0, c.banco.consultas.size());
    }

    @Test
    void etapaDesconhecidaEhRejeitada() {
        Cenario c = new Cenario();
        c.classificadorDiz("CONTRATO", 0.9);
        ConsultaRequest req = pedido("Pergunta válida");
        req.setEtapa("NAO_EXISTE");
        assertThrows(DadosInvalidosException.class, () -> c.consultaService.perguntar(c.maria.getId(), req));
    }

    @Test
    void cidadaoInexistenteDa404() {
        Cenario c = new Cenario();
        c.classificadorDiz("CONTRATO", 0.9);
        assertThrows(RecursoNaoEncontradoException.class, () -> c.consultaService.perguntar(999L, pedido("Pergunta válida")));
    }

    @Test
    void consultaDeOutraPessoaNaoEVisivel() {
        Cenario c = new Cenario();
        c.classificadorDiz("MATRICULA_ONUS", 0.9);
        Long consultaId = c.consultaService.perguntar(c.maria.getId(), pedido("O que é a matrícula do imóvel?")).getConsultaId();
        Cidadao joao = new Cidadao();
        joao.setNome("João");
        joao.setCanal(c.maria.getCanal());
        c.banco.cidadaoDao.inserir(joao);
        assertThrows(RecursoNaoEncontradoException.class, () -> c.consultaService.buscar(joao.getId(), consultaId));
        assertThrows(RecursoNaoEncontradoException.class, () -> c.consultaService.remover(joao.getId(), consultaId));
    }

    @Test
    void atualizarReprocessaADecisao() {
        Cenario c = new Cenario();
        c.classificadorDiz("CONTRATO", 0.10);
        Long id = c.consultaService.perguntar(c.maria.getId(), pedido("asdf qwer")).getConsultaId();
        assertEquals(StatusConsulta.NAO_RECONHECIDA, c.banco.consultas.get(id).getStatus());
        c.classificadorDiz("IMPOSTOS_TAXAS", 0.9);
        ConsultaRespostaResponse r = c.consultaService.atualizar(c.maria.getId(), id, pedido("O que é o ITBI e quem paga?"));
        assertEquals(StatusConsulta.RESPONDIDA, r.getStatus());
        assertEquals(c.orientItbi.getId(), c.banco.consultas.get(id).getOrientacaoId());
        assertEquals(1, c.banco.consultas.size()); // atualizou a mesma linha
    }

    @Test
    void removerApagaDoHistorico() {
        Cenario c = new Cenario();
        c.classificadorDiz("MATRICULA_ONUS", 0.9);
        Long id = c.consultaService.perguntar(c.maria.getId(), pedido("O que é a matrícula do imóvel?")).getConsultaId();
        c.consultaService.remover(c.maria.getId(), id);
        assertTrue(c.consultaService.listar(c.maria.getId()).isEmpty());
        assertFalse(c.banco.consultas.containsKey(id));
    }

    @Test
    void escolherMelhorUsaPalavrasDaPergunta() {
        Cenario c = new Cenario();
        var melhor = ConsultaService.escolherMelhor("Como saber se tem dívidas no imóvel", java.util.List.of(c.orientMatricula, c.orientDivida));
        assertEquals(c.orientDivida.getId(), melhor.getId());
    }
}
