package br.com.fiap.larouaye.service;

import br.com.fiap.larouaye.dao.CidadaoDao;
import br.com.fiap.larouaye.dto.CidadaoRequest;
import br.com.fiap.larouaye.dto.CidadaoResponse;
import br.com.fiap.larouaye.exception.RecursoNaoEncontradoException;
import br.com.fiap.larouaye.exception.RegraDeNegocioException;
import br.com.fiap.larouaye.model.Canal;
import br.com.fiap.larouaye.model.Cidadao;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/** Regras do cidadão: e-mail único, par (canal, id externo) único e proteção contra remoção com histórico. */
@Service
public class CidadaoService {

    private final CidadaoDao dao;
    private final ModelMapper mapper;

    public CidadaoService(CidadaoDao dao, ModelMapper mapper) {
        this.dao = dao;
        this.mapper = mapper;
    }

    public CidadaoResponse criar(CidadaoRequest req) {
        Cidadao c = montar(req, null);
        return paraResposta(dao.inserir(c));
    }

    public List<CidadaoResponse> listar() {
        return dao.listar().stream().map(this::paraResposta).toList();
    }

    public CidadaoResponse buscar(Long id) {
        return paraResposta(obter(id));
    }

    public CidadaoResponse atualizar(Long id, CidadaoRequest req) {
        obter(id);
        Cidadao c = montar(req, id);
        c.setId(id);
        if (!dao.atualizar(c)) {
            throw new RecursoNaoEncontradoException("Cidadão", id);
        }
        return paraResposta(obter(id));
    }

    public void remover(Long id) {
        obter(id);
        if (dao.possuiRegistros(id)) {
            throw new RegraDeNegocioException("Esta pessoa tem consultas ou progresso registrados e não pode ser removida.");
        }
        if (!dao.remover(id)) {
            throw new RecursoNaoEncontradoException("Cidadão", id);
        }
    }

    /** Usado por outros serviços para garantir que o cidadão existe (404 padronizado). */
    Cidadao obter(Long id) {
        return dao.buscarPorId(id).orElseThrow(() -> new RecursoNaoEncontradoException("Cidadão", id));
    }

    private Cidadao montar(CidadaoRequest req, Long ignorarId) {
        Cidadao c = mapper.map(req, Cidadao.class);
        c.setCanal(req.getCanal() == null ? Canal.WEB : req.getCanal());
        c.setEmail(vazioParaNulo(req.getEmail()));
        c.setIdExterno(vazioParaNulo(req.getIdExterno()));
        if (c.getEmail() != null && dao.existeEmail(c.getEmail(), ignorarId)) {
            throw new RegraDeNegocioException("Já existe uma pessoa cadastrada com esse e-mail.");
        }
        if (c.getIdExterno() != null && dao.existeCanalExterno(c.getCanal().name(), c.getIdExterno(), ignorarId)) {
            throw new RegraDeNegocioException("Esse usuário do " + c.getCanal() + " já está cadastrado.");
        }
        if (c.getCanal() == Canal.TELEGRAM && c.getIdExterno() == null) {
            throw new RegraDeNegocioException("Para o canal TELEGRAM, informe o id externo (chat).");
        }
        return c;
    }

    private CidadaoResponse paraResposta(Cidadao c) {
        return mapper.map(c, CidadaoResponse.class);
    }

    private static String vazioParaNulo(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
