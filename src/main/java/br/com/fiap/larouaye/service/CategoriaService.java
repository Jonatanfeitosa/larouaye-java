package br.com.fiap.larouaye.service;

import br.com.fiap.larouaye.dao.CategoriaDao;
import br.com.fiap.larouaye.dto.CategoriaRequest;
import br.com.fiap.larouaye.dto.CategoriaResponse;
import br.com.fiap.larouaye.exception.RecursoNaoEncontradoException;
import br.com.fiap.larouaye.exception.RegraDeNegocioException;
import br.com.fiap.larouaye.model.Categoria;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/** Regras da categoria jurídica: código único e não remover se houver orientação/consulta ligada. */
@Service
public class CategoriaService {

    private final CategoriaDao dao;
    private final ModelMapper mapper;

    public CategoriaService(CategoriaDao dao, ModelMapper mapper) {
        this.dao = dao;
        this.mapper = mapper;
    }

    public CategoriaResponse criar(CategoriaRequest req) {
        if (dao.existeCodigo(req.getCodigo(), null)) {
            throw new RegraDeNegocioException("Já existe uma categoria com o código " + req.getCodigo() + ".");
        }
        return paraResposta(dao.inserir(mapper.map(req, Categoria.class)));
    }

    public List<CategoriaResponse> listar() {
        return dao.listar().stream().map(this::paraResposta).toList();
    }

    public CategoriaResponse buscar(Long id) {
        return paraResposta(obter(id));
    }

    public CategoriaResponse atualizar(Long id, CategoriaRequest req) {
        obter(id);
        if (dao.existeCodigo(req.getCodigo(), id)) {
            throw new RegraDeNegocioException("Já existe outra categoria com o código " + req.getCodigo() + ".");
        }
        Categoria c = mapper.map(req, Categoria.class);
        c.setId(id);
        if (!dao.atualizar(c)) {
            throw new RecursoNaoEncontradoException("Categoria", id);
        }
        return paraResposta(obter(id));
    }

    public void remover(Long id) {
        obter(id);
        if (dao.possuiFilhos(id)) {
            throw new RegraDeNegocioException("Esta categoria tem orientações ou consultas ligadas e não pode ser removida.");
        }
        if (!dao.remover(id)) {
            throw new RecursoNaoEncontradoException("Categoria", id);
        }
    }

    Categoria obter(Long id) {
        return dao.buscarPorId(id).orElseThrow(() -> new RecursoNaoEncontradoException("Categoria", id));
    }

    private CategoriaResponse paraResposta(Categoria c) {
        return mapper.map(c, CategoriaResponse.class);
    }
}
