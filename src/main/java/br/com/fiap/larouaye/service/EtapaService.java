package br.com.fiap.larouaye.service;

import br.com.fiap.larouaye.dao.EtapaDao;
import br.com.fiap.larouaye.dto.EtapaDocumentoRequest;
import br.com.fiap.larouaye.dto.EtapaDocumentoResponse;
import br.com.fiap.larouaye.dto.EtapaRequest;
import br.com.fiap.larouaye.dto.EtapaResponse;
import br.com.fiap.larouaye.exception.RecursoNaoEncontradoException;
import br.com.fiap.larouaye.exception.RegraDeNegocioException;
import br.com.fiap.larouaye.model.Etapa;
import br.com.fiap.larouaye.model.EtapaDocumento;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/** Regras da etapa da compra: código e ordem únicos; não remover etapa com progresso de alguém. */
@Service
public class EtapaService {

    private final EtapaDao dao;
    private final ModelMapper mapper;

    public EtapaService(EtapaDao dao, ModelMapper mapper) {
        this.dao = dao;
        this.mapper = mapper;
    }

    public EtapaResponse criar(EtapaRequest req) {
        validarUnicidade(req, null);
        return paraResposta(dao.inserir(montar(req)));
    }

    public List<EtapaResponse> listar() {
        return dao.listar().stream().map(this::paraResposta).toList();
    }

    public EtapaResponse buscar(Long id) {
        return paraResposta(obter(id));
    }

    public List<EtapaDocumentoResponse> listarDocumentos(Long etapaId) {
        obter(etapaId);
        return dao.listarDocumentos(etapaId).stream().map(d -> mapper.map(d, EtapaDocumentoResponse.class)).toList();
    }

    public EtapaResponse atualizar(Long id, EtapaRequest req) {
        obter(id);
        validarUnicidade(req, id);
        Etapa e = montar(req);
        e.setId(id);
        if (!dao.atualizar(e)) {
            throw new RecursoNaoEncontradoException("Etapa", id);
        }
        return paraResposta(obter(id));
    }

    public void remover(Long id) {
        obter(id);
        if (dao.possuiProgresso(id)) {
            throw new RegraDeNegocioException("Há pessoas com progresso registrado nesta etapa. Ela não pode ser removida.");
        }
        if (!dao.remover(id)) {
            throw new RecursoNaoEncontradoException("Etapa", id);
        }
    }

    Etapa obter(Long id) {
        return dao.buscarPorId(id).orElseThrow(() -> new RecursoNaoEncontradoException("Etapa", id));
    }

    private void validarUnicidade(EtapaRequest req, Long ignorarId) {
        if (dao.existeCodigo(req.getCodigo(), ignorarId)) {
            throw new RegraDeNegocioException("Já existe uma etapa com o código " + req.getCodigo() + ".");
        }
        if (dao.existeOrdem(req.getOrdem(), ignorarId)) {
            throw new RegraDeNegocioException("Já existe uma etapa na posição " + req.getOrdem() + ".");
        }
    }

    private Etapa montar(EtapaRequest req) {
        Etapa e = new Etapa();
        e.setCodigo(req.getCodigo());
        e.setOrdem(req.getOrdem());
        e.setNome(req.getNome());
        e.setDescricao(req.getDescricao());
        List<EtapaDocumento> docs = new ArrayList<>();
        if (req.getDocumentos() != null) {
            for (EtapaDocumentoRequest d : req.getDocumentos()) {
                docs.add(mapper.map(d, EtapaDocumento.class));
            }
        }
        e.setDocumentos(docs);
        return e;
    }

    /** O front mostra os documentos como lista de textos simples. */
    EtapaResponse paraResposta(Etapa e) {
        EtapaResponse r = new EtapaResponse();
        r.setId(e.getId());
        r.setCodigo(e.getCodigo());
        r.setOrdem(e.getOrdem());
        r.setNome(e.getNome());
        r.setDescricao(e.getDescricao());
        List<String> nomes = new ArrayList<>();
        for (EtapaDocumento d : e.getDocumentos()) {
            nomes.add(d.getNome());
        }
        r.setDocumentos(nomes);
        return r;
    }
}
