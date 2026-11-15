package br.com.fiap.larouaye.service;

import br.com.fiap.larouaye.dao.CategoriaDao;
import br.com.fiap.larouaye.dao.OrientacaoDao;
import br.com.fiap.larouaye.dto.FonteLegalDto;
import br.com.fiap.larouaye.dto.OrientacaoRequest;
import br.com.fiap.larouaye.dto.OrientacaoResponse;
import br.com.fiap.larouaye.exception.DadosInvalidosException;
import br.com.fiap.larouaye.exception.RecursoNaoEncontradoException;
import br.com.fiap.larouaye.exception.RegraDeNegocioException;
import br.com.fiap.larouaye.model.FonteLegal;
import br.com.fiap.larouaye.model.Orientacao;
import br.com.fiap.larouaye.model.StatusRevisao;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Regras do conteúdo jurídico. Principal regra de negócio: orientação marcada como REVISADO precisa
 * de data de revisão e de pelo menos uma fonte legal. Assim nada vira "verdade jurídica" sem base.
 */
@Service
public class OrientacaoService {

    private final OrientacaoDao dao;
    private final CategoriaDao categoriaDao;
    private final ModelMapper mapper;

    public OrientacaoService(OrientacaoDao dao, CategoriaDao categoriaDao, ModelMapper mapper) {
        this.dao = dao;
        this.categoriaDao = categoriaDao;
        this.mapper = mapper;
    }

    public OrientacaoResponse criar(OrientacaoRequest req) {
        return paraResposta(dao.inserir(montar(req)));
    }

    public List<OrientacaoResponse> listar() {
        return dao.listar().stream().map(this::paraResposta).toList();
    }

    public List<OrientacaoResponse> listarPorCategoria(Long categoriaId) {
        categoriaDao.buscarPorId(categoriaId).orElseThrow(() -> new RecursoNaoEncontradoException("Categoria", categoriaId));
        return dao.listarPorCategoria(categoriaId).stream().map(this::paraResposta).toList();
    }

    public OrientacaoResponse buscar(Long id) {
        return paraResposta(obter(id));
    }

    public OrientacaoResponse atualizar(Long id, OrientacaoRequest req) {
        obter(id);
        Orientacao o = montar(req);
        o.setId(id);
        if (!dao.atualizar(o)) {
            throw new RecursoNaoEncontradoException("Orientação", id);
        }
        return paraResposta(obter(id));
    }

    public void remover(Long id) {
        obter(id);
        if (dao.possuiConsultas(id)) {
            throw new RegraDeNegocioException("Esta orientação já foi entregue em consultas. Em vez de remover, desative-a (ativo = false).");
        }
        if (!dao.remover(id)) {
            throw new RecursoNaoEncontradoException("Orientação", id);
        }
    }

    Orientacao obter(Long id) {
        return dao.buscarPorId(id).orElseThrow(() -> new RecursoNaoEncontradoException("Orientação", id));
    }

    /** DTO -> model: escalares pelo ModelMapper; listas e padrões à mão (mais claro e seguro). */
    private Orientacao montar(OrientacaoRequest req) {
        categoriaDao.buscarPorId(req.getCategoriaId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Categoria", req.getCategoriaId()));
        Orientacao o = mapper.map(req, Orientacao.class);
        o.setRevisao(req.getRevisao() == null ? StatusRevisao.PENDENTE : req.getRevisao());
        o.setAtivo(req.getAtivo() == null || req.getAtivo());
        o.setCuidados(limparCuidados(req.getCuidados()));
        o.setBaseLegal(converterFontes(req.getBaseLegal()));
        validarRevisao(o);
        return o;
    }

    private static void validarRevisao(Orientacao o) {
        if (o.getRevisao() == StatusRevisao.REVISADO) {
            if (o.getDataRevisao() == null) {
                throw new DadosInvalidosException("Para marcar como REVISADO, informe a data da revisão.");
            }
            if (o.getBaseLegal().isEmpty()) {
                throw new RegraDeNegocioException("Para marcar como REVISADO, informe ao menos uma fonte legal.");
            }
        } else if (o.getDataRevisao() != null && o.getDataRevisao().isAfter(LocalDate.now())) {
            throw new DadosInvalidosException("A data de revisão não pode ser futura.");
        }
    }

    private static List<String> limparCuidados(List<String> cuidados) {
        List<String> saida = new ArrayList<>();
        if (cuidados != null) {
            for (String c : cuidados) {
                if (c != null && !c.isBlank()) {
                    saida.add(c.trim());
                }
            }
        }
        return saida;
    }

    private List<FonteLegal> converterFontes(List<FonteLegalDto> dtos) {
        List<FonteLegal> saida = new ArrayList<>();
        if (dtos != null) {
            for (FonteLegalDto d : dtos) {
                saida.add(mapper.map(d, FonteLegal.class));
            }
        }
        return saida;
    }

    OrientacaoResponse paraResposta(Orientacao o) {
        OrientacaoResponse r = mapper.map(o, OrientacaoResponse.class);
        r.setCategoria(o.getCategoriaCodigo());
        r.setCuidados(new ArrayList<>(o.getCuidados()));
        List<FonteLegalDto> fontes = new ArrayList<>();
        for (FonteLegal f : o.getBaseLegal()) {
            fontes.add(mapper.map(f, FonteLegalDto.class));
        }
        r.setBaseLegal(fontes);
        return r;
    }
}
