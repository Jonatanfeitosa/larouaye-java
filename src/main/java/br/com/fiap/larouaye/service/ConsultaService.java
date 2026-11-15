package br.com.fiap.larouaye.service;

import br.com.fiap.larouaye.classificador.ClassificadorPort;
import br.com.fiap.larouaye.classificador.ResultadoClassificacao;
import br.com.fiap.larouaye.dao.CategoriaDao;
import br.com.fiap.larouaye.dao.ConsultaDao;
import br.com.fiap.larouaye.dao.EtapaDao;
import br.com.fiap.larouaye.dao.OrientacaoDao;
import br.com.fiap.larouaye.dto.ConsultaRequest;
import br.com.fiap.larouaye.dto.ConsultaRespostaResponse;
import br.com.fiap.larouaye.dto.ConsultaResumoResponse;
import br.com.fiap.larouaye.dto.FonteLegalDto;
import br.com.fiap.larouaye.dto.RespostaConteudo;
import br.com.fiap.larouaye.classificador.ClassificadorPorRegras;
import br.com.fiap.larouaye.exception.DadosInvalidosException;
import br.com.fiap.larouaye.exception.RecursoNaoEncontradoException;
import br.com.fiap.larouaye.model.Canal;
import br.com.fiap.larouaye.model.Categoria;
import br.com.fiap.larouaye.model.Consulta;
import br.com.fiap.larouaye.model.Etapa;
import br.com.fiap.larouaye.model.FonteLegal;
import br.com.fiap.larouaye.model.Orientacao;
import br.com.fiap.larouaye.model.StatusConsulta;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Coração do sistema: "A IA identifica. O Java decide. O conteúdo jurídico fundamenta."
 *
 * <p>A classificação é só uma sugestão. A decisão segue regras escritas aqui, em um lugar só:
 * <ul>
 *   <li>confiança &gt;= limiar de resposta e existe orientação ativa na categoria: RESPONDIDA;</li>
 *   <li>confiança intermediária: ESCLARECIMENTO (devolve opções, não chuta);</li>
 *   <li>fora do escopo, confiança baixa, ou categoria sem orientação revisada: NAO_RECONHECIDA.</li>
 * </ul>
 * Toda consulta é gravada (histórico), mesmo quando não foi reconhecida.
 */
@Service
public class ConsultaService {

    private static final int MAX_OPCOES = 3;

    private final ConsultaDao dao;
    private final CategoriaDao categoriaDao;
    private final OrientacaoDao orientacaoDao;
    private final EtapaDao etapaDao;
    private final CidadaoService cidadaoService;
    private final ClassificadorPort classificador;
    private final ModelMapper mapper;
    private final double limiarResponder;
    private final double limiarEsclarecer;

    public ConsultaService(ConsultaDao dao, CategoriaDao categoriaDao, OrientacaoDao orientacaoDao, EtapaDao etapaDao,
                           CidadaoService cidadaoService, ClassificadorPort classificador, ModelMapper mapper,
                           @Value("${larouaye.decisao.limiar-responder}") double limiarResponder,
                           @Value("${larouaye.decisao.limiar-esclarecer}") double limiarEsclarecer) {
        this.dao = dao;
        this.categoriaDao = categoriaDao;
        this.orientacaoDao = orientacaoDao;
        this.etapaDao = etapaDao;
        this.cidadaoService = cidadaoService;
        this.classificador = classificador;
        this.mapper = mapper;
        this.limiarResponder = limiarResponder;
        this.limiarEsclarecer = limiarEsclarecer;
    }

    // ------------------------------------------------------------ casos de uso
    /** POST: recebe a dúvida, decide, grava o histórico e devolve a orientação (ou o pedido de esclarecimento). */
    public ConsultaRespostaResponse perguntar(Long cidadaoId, ConsultaRequest req) {
        cidadaoService.obter(cidadaoId);
        Consulta nova = new Consulta();
        nova.setCidadaoId(cidadaoId);
        Decisao d = decidir(req, nova);
        return montarResposta(dao.inserir(nova), d, req.getEtapa());
    }

    public List<ConsultaResumoResponse> listar(Long cidadaoId) {
        cidadaoService.obter(cidadaoId);
        return dao.listarPorCidadao(cidadaoId).stream().map(this::resumir).toList();
    }

    public ConsultaResumoResponse buscar(Long cidadaoId, Long consultaId) {
        return resumir(obterDoCidadao(cidadaoId, consultaId));
    }

    /** PUT: corrige a pergunta/UF/etapa e REPROCESSA a decisão (a resposta antiga deixa de valer). */
    public ConsultaRespostaResponse atualizar(Long cidadaoId, Long consultaId, ConsultaRequest req) {
        Consulta existente = obterDoCidadao(cidadaoId, consultaId);
        Decisao d = decidir(req, existente);
        if (!dao.atualizar(existente)) {
            throw new RecursoNaoEncontradoException("Consulta", consultaId);
        }
        return montarResposta(obterDoCidadao(cidadaoId, consultaId), d, req.getEtapa());
    }

    public void remover(Long cidadaoId, Long consultaId) {
        obterDoCidadao(cidadaoId, consultaId);
        if (!dao.remover(consultaId)) {
            throw new RecursoNaoEncontradoException("Consulta", consultaId);
        }
    }

    // ------------------------------------------------------------ decisão
    /** Resultado interno da decisão. */
    private record Decisao(StatusConsulta status, Orientacao orientacao, Categoria categoria, List<String> opcoes, String mensagem) {
    }

    private Decisao decidir(ConsultaRequest req, Consulta alvo) {
        String uf = Ufs.normalizar(req.getUf());
        if (!Ufs.valida(uf)) {
            throw new DadosInvalidosException("UF inválida. Use a sigla do estado, por exemplo SP.");
        }
        String etapa = req.getEtapa() == null || req.getEtapa().isBlank() ? null : req.getEtapa().trim();
        if (etapa != null) {
            etapaDao.buscarPorCodigo(etapa).orElseThrow(() -> new DadosInvalidosException("Etapa desconhecida: " + etapa + "."));
        }

        alvo.setPergunta(req.getPergunta().trim());
        alvo.setUf(uf);
        alvo.setEtapaAtual(etapa);
        alvo.setCanal(req.getCanal() == null ? Canal.WEB : req.getCanal());
        alvo.setCategoriaId(null);
        alvo.setOrientacaoId(null);

        // 1) Se a pessoa já escolheu uma categoria (resposta a um esclarecimento), vale a escolha dela.
        Categoria escolhida = resolverCategoriaEscolhida(req.getCategoria());
        ResultadoClassificacao r = escolhida != null ? null : classificador.classificar(alvo.getPergunta());
        double confianca = escolhida != null ? 1.0 : r.confianca();
        alvo.setConfianca(BigDecimal.valueOf(Math.max(0, Math.min(1, confianca))).setScale(2, RoundingMode.HALF_UP));

        Decisao decisao;
        if (escolhida != null) {
            decisao = responderComCategorias(alvo, List.of(escolhida), 1.0);
        } else if (MapaCategorias.FORA_DO_ESCOPO.equals(r.categoria())) {
            decisao = naoReconhecida("Eu só ajudo com a compra do primeiro imóvel. Pergunte sobre matrícula, contrato, escritura, registro, impostos ou financiamento.");
        } else if (confianca >= limiarResponder) {
            decisao = responderComCategorias(alvo, categoriasDoBanco(List.of(r.categoria())), confianca);
        } else if (confianca >= limiarEsclarecer) {
            decisao = pedirEsclarecimento(r);
        } else {
            decisao = naoReconhecida("Ainda não tenho uma orientação segura para essa pergunta.");
        }
        alvo.setStatus(decisao.status());
        if (decisao.categoria() != null) {
            alvo.setCategoriaId(decisao.categoria().getId());
        }
        if (decisao.orientacao() != null) {
            alvo.setOrientacaoId(decisao.orientacao().getId());
        }
        return decisao;
    }

    private Decisao responderComCategorias(Consulta alvo, List<Categoria> categorias, double confianca) {
        if (categorias.isEmpty()) {
            return naoReconhecida("Esse assunto ainda não tem orientação revisada aqui. Procure o cartório, a Defensoria Pública ou um advogado.");
        }
        List<Long> ids = categorias.stream().map(Categoria::getId).toList();
        List<Orientacao> candidatas = orientacaoDao.listarAtivasPorCategorias(ids);
        if (candidatas.isEmpty()) {
            return naoReconhecida("Ainda não temos orientação cadastrada para esse assunto.");
        }
        Orientacao melhor = escolherMelhor(alvo.getPergunta(), candidatas);
        Categoria categoria = categorias.stream().filter(c -> c.getId().equals(melhor.getCategoriaId())).findFirst().orElse(categorias.get(0));
        return new Decisao(StatusConsulta.RESPONDIDA, melhor, categoria, List.of(), null);
    }

    private Decisao pedirEsclarecimento(ResultadoClassificacao r) {
        Set<String> nomes = new LinkedHashSet<>();
        for (ResultadoClassificacao.Candidata c : r.maisProvaveis()) {
            for (Categoria cat : categoriasDoBanco(List.of(c.categoria()))) {
                nomes.add(cat.getNome());
            }
            if (nomes.size() >= MAX_OPCOES) {
                break;
            }
        }
        if (nomes.isEmpty()) {
            return naoReconhecida("Ainda não tenho uma orientação segura para essa pergunta.");
        }
        List<String> opcoes = new ArrayList<>(nomes).subList(0, Math.min(MAX_OPCOES, nomes.size()));
        return new Decisao(StatusConsulta.ESCLARECIMENTO, null, null, opcoes, "Não tenho certeza do que você quer saber. Escolha um assunto.");
    }

    private static Decisao naoReconhecida(String mensagem) {
        return new Decisao(StatusConsulta.NAO_RECONHECIDA, null, null, List.of(), mensagem);
    }

    // ------------------------------------------------------------ apoio
    private List<Categoria> categoriasDoBanco(List<String> rotulosModelo) {
        List<Categoria> lista = new ArrayList<>();
        for (String rotulo : rotulosModelo) {
            for (String codigo : MapaCategorias.paraBanco(rotulo)) {
                categoriaDao.buscarPorCodigo(codigo).ifPresent(lista::add);
            }
        }
        return lista;
    }

    /** Aceita o código do banco, o nome da categoria ou o rótulo do modelo; vazio/desconhecido vira "nenhuma escolha". */
    private Categoria resolverCategoriaEscolhida(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        String chave = ClassificadorPorRegras.normalizar(texto.trim());
        for (Categoria c : categoriaDao.listar()) {
            if (ClassificadorPorRegras.normalizar(c.getCodigo()).equals(chave) || ClassificadorPorRegras.normalizar(c.getNome()).equals(chave)) {
                return c;
            }
        }
        List<Categoria> pelaIa = categoriasDoBanco(List.of(texto.trim().toUpperCase(java.util.Locale.ROOT)));
        return pelaIa.isEmpty() ? null : pelaIa.get(0);
    }

    /** Entre as orientações candidatas, a que mais compartilha palavras com a pergunta (empate: a primeira). */
    static Orientacao escolherMelhor(String pergunta, List<Orientacao> candidatas) {
        Set<String> palavras = palavrasUteis(pergunta);
        Orientacao melhor = candidatas.get(0);
        int melhorPontos = -1;
        for (Orientacao o : candidatas) {
            Set<String> alvo = palavrasUteis(o.getTitulo() + " " + o.getPerguntaRelacionada());
            int pontos = 0;
            for (String p : palavras) {
                if (alvo.contains(p)) {
                    pontos++;
                }
            }
            if (pontos > melhorPontos) {
                melhorPontos = pontos;
                melhor = o;
            }
        }
        return melhor;
    }

    private static Set<String> palavrasUteis(String texto) {
        Set<String> saida = new LinkedHashSet<>();
        for (String p : ClassificadorPorRegras.normalizar(texto).split("[^a-z0-9]+")) {
            if (p.length() >= 4) {
                saida.add(p);
            }
        }
        return saida;
    }

    private Consulta obterDoCidadao(Long cidadaoId, Long consultaId) {
        cidadaoService.obter(cidadaoId);
        Consulta c = dao.buscarPorId(consultaId).orElseThrow(() -> new RecursoNaoEncontradoException("Consulta", consultaId));
        if (!c.getCidadaoId().equals(cidadaoId)) {
            // Não revela que a consulta existe para outra pessoa.
            throw new RecursoNaoEncontradoException("Consulta", consultaId);
        }
        return c;
    }

    private ConsultaResumoResponse resumir(Consulta c) {
        ConsultaResumoResponse r = mapper.map(c, ConsultaResumoResponse.class);
        r.setConsultaId(c.getId());
        r.setCategoria(c.getCategoriaCodigo());
        return r;
    }

    private ConsultaRespostaResponse montarResposta(Consulta salva, Decisao d, String etapaAtual) {
        ConsultaRespostaResponse r = new ConsultaRespostaResponse();
        r.setConsultaId(salva.getId());
        r.setStatus(d.status());
        r.setCategoria(d.categoria() == null ? null : d.categoria().getCodigo());
        r.setMensagem(d.mensagem());
        r.setOpcoes(d.opcoes().isEmpty() ? null : d.opcoes());
        if (d.orientacao() != null) {
            r.setResposta(paraConteudo(d.orientacao()));
            r.setProximaEtapa(proximaEtapa(etapaAtual));
        }
        return r;
    }

    private RespostaConteudo paraConteudo(Orientacao o) {
        RespostaConteudo c = new RespostaConteudo();
        c.setOrientacaoId(o.getId());
        c.setTitulo(o.getTitulo());
        c.setConteudo(o.getConteudo());
        c.setCuidados(new ArrayList<>(o.getCuidados()));
        c.setDataRevisao(o.getDataRevisao());
        List<FonteLegalDto> fontes = new ArrayList<>();
        for (FonteLegal f : o.getBaseLegal()) {
            fontes.add(mapper.map(f, FonteLegalDto.class));
        }
        c.setBaseLegal(fontes);
        return c;
    }

    /** Nome da etapa seguinte à atual, para a pessoa saber "o que vem depois". Nulo se não houver. */
    private String proximaEtapa(String codigoAtual) {
        if (codigoAtual == null || codigoAtual.isBlank()) {
            return null;
        }
        List<Etapa> etapas = etapaDao.listar();
        for (int i = 0; i < etapas.size() - 1; i++) {
            if (etapas.get(i).getCodigo().equals(codigoAtual.trim())) {
                return etapas.get(i + 1).getNome();
            }
        }
        return null;
    }
}
