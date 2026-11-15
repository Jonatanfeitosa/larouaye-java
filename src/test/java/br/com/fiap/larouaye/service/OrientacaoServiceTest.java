package br.com.fiap.larouaye.service;

import br.com.fiap.larouaye.dto.FonteLegalDto;
import br.com.fiap.larouaye.dto.OrientacaoRequest;
import br.com.fiap.larouaye.dto.OrientacaoResponse;
import br.com.fiap.larouaye.exception.DadosInvalidosException;
import br.com.fiap.larouaye.exception.RecursoNaoEncontradoException;
import br.com.fiap.larouaye.exception.RegraDeNegocioException;
import br.com.fiap.larouaye.model.StatusConsulta;
import br.com.fiap.larouaye.model.StatusRevisao;
import br.com.fiap.larouaye.model.Consulta;
import br.com.fiap.larouaye.model.Canal;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrientacaoServiceTest {

    private OrientacaoRequest pedido(Cenario c) {
        OrientacaoRequest r = new OrientacaoRequest();
        r.setCategoriaId(c.documentacao.getId());
        r.setTitulo("Título de teste");
        r.setPerguntaRelacionada("Pergunta de teste?");
        r.setConteudo("Conteúdo de teste com mais de dez letras.");
        r.setCuidados(List.of("Primeiro cuidado", "  ", "Segundo cuidado"));
        return r;
    }

    private FonteLegalDto fonte() {
        FonteLegalDto f = new FonteLegalDto();
        f.setNorma("Lei 6.015/1973");
        f.setReferencia("Lei de Registros Públicos");
        return f;
    }

    @Test
    void criaComPadroesELimpaCuidadosVazios() {
        Cenario c = new Cenario();
        OrientacaoResponse r = c.orientacaoService.criar(pedido(c));
        assertEquals(StatusRevisao.PENDENTE, r.getRevisao());
        assertTrue(r.isAtivo());
        assertEquals(2, r.getCuidados().size());
        assertEquals("DOCUMENTACAO", r.getCategoria());
    }

    @Test
    void revisadoExigeDataDaRevisao() {
        Cenario c = new Cenario();
        OrientacaoRequest req = pedido(c);
        req.setRevisao(StatusRevisao.REVISADO);
        req.setBaseLegal(List.of(fonte()));
        assertThrows(DadosInvalidosException.class, () -> c.orientacaoService.criar(req));
    }

    @Test
    void revisadoExigeAoMenosUmaFonteLegal() {
        Cenario c = new Cenario();
        OrientacaoRequest req = pedido(c);
        req.setRevisao(StatusRevisao.REVISADO);
        req.setDataRevisao(LocalDate.now());
        assertThrows(RegraDeNegocioException.class, () -> c.orientacaoService.criar(req));
    }

    @Test
    void revisadoComDataEFonteEhAceito() {
        Cenario c = new Cenario();
        OrientacaoRequest req = pedido(c);
        req.setRevisao(StatusRevisao.REVISADO);
        req.setDataRevisao(LocalDate.now());
        req.setBaseLegal(List.of(fonte()));
        OrientacaoResponse r = c.orientacaoService.criar(req);
        assertEquals(StatusRevisao.REVISADO, r.getRevisao());
        assertEquals(1, r.getBaseLegal().size());
    }

    @Test
    void categoriaInexistenteDa404() {
        Cenario c = new Cenario();
        OrientacaoRequest req = pedido(c);
        req.setCategoriaId(9999L);
        assertThrows(RecursoNaoEncontradoException.class, () -> c.orientacaoService.criar(req));
    }

    @Test
    void naoRemoveOrientacaoJaEntregueEmConsulta() {
        Cenario c = new Cenario();
        Consulta q = new Consulta();
        q.setCidadaoId(c.maria.getId());
        q.setOrientacaoId(c.orientMatricula.getId());
        q.setPergunta("x");
        q.setUf("SP");
        q.setStatus(StatusConsulta.RESPONDIDA);
        q.setCanal(Canal.WEB);
        c.banco.consultaDao.inserir(q);
        assertThrows(RegraDeNegocioException.class, () -> c.orientacaoService.remover(c.orientMatricula.getId()));
    }

    @Test
    void removeOrientacaoSemConsultas() {
        Cenario c = new Cenario();
        c.orientacaoService.remover(c.orientItbi.getId());
        assertThrows(RecursoNaoEncontradoException.class, () -> c.orientacaoService.buscar(c.orientItbi.getId()));
    }

    @Test
    void listaPorCategoriaEExigeCategoriaExistente() {
        Cenario c = new Cenario();
        assertEquals(2, c.orientacaoService.listarPorCategoria(c.documentacao.getId()).size());
        assertThrows(RecursoNaoEncontradoException.class, () -> c.orientacaoService.listarPorCategoria(9999L));
    }
}
