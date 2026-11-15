package br.com.fiap.larouaye.service;

import br.com.fiap.larouaye.classificador.ClassificadorPort;
import br.com.fiap.larouaye.classificador.ResultadoClassificacao;
import br.com.fiap.larouaye.fake.FakeDaos;
import br.com.fiap.larouaye.model.Categoria;
import br.com.fiap.larouaye.model.Cidadao;
import br.com.fiap.larouaye.model.Canal;
import br.com.fiap.larouaye.model.Etapa;
import br.com.fiap.larouaye.model.Orientacao;
import br.com.fiap.larouaye.model.StatusRevisao;
import org.modelmapper.ModelMapper;

import java.util.ArrayList;
import java.util.List;

/** Monta serviços reais com DAOs falsos e um classificador "de mentira" que devolve o que o teste mandar. */
class Cenario {

    final FakeDaos.Banco banco = new FakeDaos.Banco();
    final ModelMapper mapper = new ModelMapper();
    ResultadoClassificacao proximoResultado;

    final ClassificadorPort classificador = pergunta -> proximoResultado;

    final CidadaoService cidadaoService = new CidadaoService(banco.cidadaoDao, mapper);
    final CategoriaService categoriaService = new CategoriaService(banco.categoriaDao, mapper);
    final OrientacaoService orientacaoService = new OrientacaoService(banco.orientacaoDao, banco.categoriaDao, mapper);
    final EtapaService etapaService = new EtapaService(banco.etapaDao, mapper);
    final ProgressoService progressoService = new ProgressoService(banco.progressoDao, banco.etapaDao, cidadaoService, mapper);
    final ConsultaService consultaService = new ConsultaService(banco.consultaDao, banco.categoriaDao, banco.orientacaoDao,
            banco.etapaDao, cidadaoService, classificador, mapper, 0.70, 0.30);

    Categoria documentacao;
    Categoria escritura;
    Categoria tributos;
    Orientacao orientDivida;
    Orientacao orientMatricula;
    Orientacao orientItbi;
    Cidadao maria;
    Etapa etapaDocumentos;
    Etapa etapaContrato;

    Cenario() {
        documentacao = categoria("DOCUMENTACAO", "Documentação");
        escritura = categoria("ESCRITURA", "Escritura");
        tributos = categoria("TRIBUTOS", "Impostos e taxas");
        orientMatricula = orientacao(documentacao, "O que é a matrícula do imóvel", "O que é a matrícula do imóvel?");
        orientDivida = orientacao(documentacao, "Como saber se o imóvel tem dívidas", "Como descubro se um imóvel tem dívidas?");
        orientItbi = orientacao(tributos, "O que é o ITBI", "O que é o ITBI e quem paga?");
        etapaDocumentos = etapa("ANALISE_DOCUMENTAL", 2, "Análise documental");
        etapaContrato = etapa("NEGOCIACAO_CONTRATO", 3, "Negociação e contrato");
        maria = new Cidadao();
        maria.setNome("Maria");
        maria.setCanal(Canal.WEB);
        banco.cidadaoDao.inserir(maria);
    }

    Categoria categoria(String codigo, String nome) {
        Categoria c = new Categoria();
        c.setCodigo(codigo);
        c.setNome(nome);
        return banco.categoriaDao.inserir(c);
    }

    Orientacao orientacao(Categoria cat, String titulo, String pergunta) {
        Orientacao o = new Orientacao();
        o.setCategoriaId(cat.getId());
        o.setTitulo(titulo);
        o.setPerguntaRelacionada(pergunta);
        o.setConteudo("Conteúdo de teste com mais de dez letras.");
        o.setRevisao(StatusRevisao.PENDENTE);
        o.setAtivo(true);
        o.setCuidados(new ArrayList<>(List.of("Cuidado 1")));
        o.setBaseLegal(new ArrayList<>());
        return banco.orientacaoDao.inserir(o);
    }

    Etapa etapa(String codigo, int ordem, String nome) {
        Etapa e = new Etapa();
        e.setCodigo(codigo);
        e.setOrdem(ordem);
        e.setNome(nome);
        e.setDescricao("Descrição");
        e.setDocumentos(new ArrayList<>());
        return banco.etapaDao.inserir(e);
    }

    /** Define o que o "classificador" vai responder na próxima pergunta. */
    void classificadorDiz(String categoria, double confianca, String... outras) {
        List<ResultadoClassificacao.Candidata> lista = new ArrayList<>();
        lista.add(new ResultadoClassificacao.Candidata(categoria, confianca));
        for (String o : outras) {
            lista.add(new ResultadoClassificacao.Candidata(o, 0.2));
        }
        proximoResultado = new ResultadoClassificacao(categoria, confianca, lista, null, "TESTE");
    }
}
