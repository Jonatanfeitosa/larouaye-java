package br.com.fiap.larouaye.service;

import br.com.fiap.larouaye.dto.CategoriaRequest;
import br.com.fiap.larouaye.dto.CidadaoRequest;
import br.com.fiap.larouaye.dto.CidadaoResponse;
import br.com.fiap.larouaye.dto.EtapaDocumentoRequest;
import br.com.fiap.larouaye.dto.EtapaRequest;
import br.com.fiap.larouaye.dto.EtapaResponse;
import br.com.fiap.larouaye.dto.ProgressoRequest;
import br.com.fiap.larouaye.dto.ProgressoResponse;
import br.com.fiap.larouaye.exception.DadosInvalidosException;
import br.com.fiap.larouaye.exception.RecursoNaoEncontradoException;
import br.com.fiap.larouaye.exception.RegraDeNegocioException;
import br.com.fiap.larouaye.model.Canal;
import br.com.fiap.larouaye.model.StatusEtapa;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Regras dos cadastros simples: unicidade (409), 404 e proteção contra remoção com vínculos. */
class CadastrosServiceTest {

    // ---------- cidadão ----------
    private CidadaoRequest cidadao(String nome, String email) {
        CidadaoRequest r = new CidadaoRequest();
        r.setNome(nome);
        r.setEmail(email);
        return r;
    }

    @Test
    void cidadaoNovoUsaCanalWebPorPadrao() {
        Cenario c = new Cenario();
        CidadaoResponse r = c.cidadaoService.criar(cidadao("Ana", "ana@exemplo.com"));
        assertEquals(Canal.WEB, r.getCanal());
    }

    @Test
    void emailDuplicadoDa409() {
        Cenario c = new Cenario();
        c.cidadaoService.criar(cidadao("Ana", "ana@exemplo.com"));
        assertThrows(RegraDeNegocioException.class, () -> c.cidadaoService.criar(cidadao("Outra Ana", "ANA@exemplo.com")));
    }

    @Test
    void emailEmBrancoNaoContaComoDuplicado() {
        Cenario c = new Cenario();
        c.cidadaoService.criar(cidadao("Ana", " "));
        c.cidadaoService.criar(cidadao("Beto", ""));
        assertEquals(3, c.cidadaoService.listar().size()); // Maria do cenário + 2
    }

    @Test
    void telegramExigeIdExterno() {
        Cenario c = new Cenario();
        CidadaoRequest r = cidadao("Telegram", null);
        r.setCanal(Canal.TELEGRAM);
        assertThrows(RegraDeNegocioException.class, () -> c.cidadaoService.criar(r));
    }

    @Test
    void mesmoChatDoTelegramNaoCadastraDuasVezes() {
        Cenario c = new Cenario();
        CidadaoRequest r = cidadao("Telegram", null);
        r.setCanal(Canal.TELEGRAM);
        r.setIdExterno("123456");
        c.cidadaoService.criar(r);
        assertThrows(RegraDeNegocioException.class, () -> c.cidadaoService.criar(r));
    }

    @Test
    void atualizarMantemOProprioEmailSemConflito() {
        Cenario c = new Cenario();
        CidadaoResponse criada = c.cidadaoService.criar(cidadao("Ana", "ana@exemplo.com"));
        CidadaoResponse r = c.cidadaoService.atualizar(criada.getId(), cidadao("Ana Maria", "ana@exemplo.com"));
        assertEquals("Ana Maria", r.getNome());
    }

    @Test
    void cidadaoComHistoricoNaoPodeSerRemovido() {
        Cenario c = new Cenario();
        c.classificadorDiz("MATRICULA_ONUS", 0.9);
        var req = new br.com.fiap.larouaye.dto.ConsultaRequest();
        req.setPergunta("O que é a matrícula do imóvel?");
        req.setUf("SP");
        c.consultaService.perguntar(c.maria.getId(), req);
        assertThrows(RegraDeNegocioException.class, () -> c.cidadaoService.remover(c.maria.getId()));
    }

    @Test
    void cidadaoSemVinculosPodeSerRemovido() {
        Cenario c = new Cenario();
        c.cidadaoService.remover(c.maria.getId());
        assertThrows(RecursoNaoEncontradoException.class, () -> c.cidadaoService.buscar(c.maria.getId()));
    }

    // ---------- categoria ----------
    private CategoriaRequest categoria(String codigo) {
        CategoriaRequest r = new CategoriaRequest();
        r.setCodigo(codigo);
        r.setNome("Nome " + codigo);
        return r;
    }

    @Test
    void codigoDeCategoriaDuplicadoDa409() {
        Cenario c = new Cenario();
        assertThrows(RegraDeNegocioException.class, () -> c.categoriaService.criar(categoria("DOCUMENTACAO")));
    }

    @Test
    void categoriaComOrientacaoNaoPodeSerRemovida() {
        Cenario c = new Cenario();
        assertThrows(RegraDeNegocioException.class, () -> c.categoriaService.remover(c.documentacao.getId()));
    }

    @Test
    void categoriaSemFilhosPodeSerRemovida() {
        Cenario c = new Cenario();
        c.categoriaService.remover(c.escritura.getId());
        assertThrows(RecursoNaoEncontradoException.class, () -> c.categoriaService.buscar(c.escritura.getId()));
    }

    // ---------- etapa ----------
    private EtapaRequest etapa(String codigo, int ordem) {
        EtapaRequest r = new EtapaRequest();
        r.setCodigo(codigo);
        r.setOrdem(ordem);
        r.setNome("Etapa " + codigo);
        r.setDescricao("Descrição da etapa");
        EtapaDocumentoRequest d = new EtapaDocumentoRequest();
        d.setNome("Documento A");
        r.setDocumentos(List.of(d));
        return r;
    }

    @Test
    void etapaDevolveDocumentosComoTextos() {
        Cenario c = new Cenario();
        EtapaResponse r = c.etapaService.criar(etapa("NOVA_ETAPA", 9));
        assertEquals(List.of("Documento A"), r.getDocumentos());
    }

    @Test
    void ordemOuCodigoDeEtapaDuplicadosDao409() {
        Cenario c = new Cenario();
        assertThrows(RegraDeNegocioException.class, () -> c.etapaService.criar(etapa("OUTRA", 2)));
        assertThrows(RegraDeNegocioException.class, () -> c.etapaService.criar(etapa("ANALISE_DOCUMENTAL", 8)));
    }

    @Test
    void etapaComProgressoNaoPodeSerRemovida() {
        Cenario c = new Cenario();
        ProgressoRequest p = new ProgressoRequest();
        p.setEtapaId(c.etapaDocumentos.getId());
        p.setStatus(StatusEtapa.EM_ANDAMENTO);
        c.progressoService.salvar(c.maria.getId(), p);
        assertThrows(RegraDeNegocioException.class, () -> c.etapaService.remover(c.etapaDocumentos.getId()));
    }

    // ---------- progresso ----------
    @Test
    void salvarProgressoDuasVezesNaoDuplica() {
        Cenario c = new Cenario();
        ProgressoRequest p = new ProgressoRequest();
        p.setEtapaId(c.etapaDocumentos.getId());
        p.setStatus(StatusEtapa.EM_ANDAMENTO);
        c.progressoService.salvar(c.maria.getId(), p);
        p.setStatus(StatusEtapa.CONCLUIDA);
        ProgressoResponse r = c.progressoService.salvar(c.maria.getId(), p);
        assertEquals(StatusEtapa.CONCLUIDA, r.getStatus());
        assertEquals(1, c.progressoService.listar(c.maria.getId()).size());
    }

    @Test
    void progressoComEtapaOuCidadaoInexistenteDa404() {
        Cenario c = new Cenario();
        ProgressoRequest p = new ProgressoRequest();
        p.setEtapaId(9999L);
        p.setStatus(StatusEtapa.PENDENTE);
        assertThrows(RecursoNaoEncontradoException.class, () -> c.progressoService.salvar(c.maria.getId(), p));
        p.setEtapaId(c.etapaDocumentos.getId());
        assertThrows(RecursoNaoEncontradoException.class, () -> c.progressoService.salvar(9999L, p));
    }

    // ---------- UF ----------
    @Test
    void ufInvalidaNoGuiaDeServicosDa400() {
        var servico = new ServicoService(uf -> List.of(), new org.modelmapper.ModelMapper());
        assertThrows(DadosInvalidosException.class, () -> servico.listarPorUf("XX"));
        assertEquals(0, servico.listarPorUf(" sp ").size());
    }
}
