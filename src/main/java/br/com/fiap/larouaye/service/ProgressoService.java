package br.com.fiap.larouaye.service;

import br.com.fiap.larouaye.dao.EtapaDao;
import br.com.fiap.larouaye.dao.ProgressoDao;
import br.com.fiap.larouaye.dto.ProgressoRequest;
import br.com.fiap.larouaye.dto.ProgressoResponse;
import br.com.fiap.larouaye.exception.RecursoNaoEncontradoException;
import br.com.fiap.larouaye.model.Progresso;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/** Progresso do cidadão nas etapas (N:N). Gravar duas vezes a mesma etapa atualiza, não duplica. */
@Service
public class ProgressoService {

    private final ProgressoDao dao;
    private final EtapaDao etapaDao;
    private final CidadaoService cidadaoService;
    private final ModelMapper mapper;

    public ProgressoService(ProgressoDao dao, EtapaDao etapaDao, CidadaoService cidadaoService, ModelMapper mapper) {
        this.dao = dao;
        this.etapaDao = etapaDao;
        this.cidadaoService = cidadaoService;
        this.mapper = mapper;
    }

    public List<ProgressoResponse> listar(Long cidadaoId) {
        cidadaoService.obter(cidadaoId);
        return dao.listarPorCidadao(cidadaoId).stream().map(p -> mapper.map(p, ProgressoResponse.class)).toList();
    }

    public ProgressoResponse salvar(Long cidadaoId, ProgressoRequest req) {
        cidadaoService.obter(cidadaoId);
        etapaDao.buscarPorId(req.getEtapaId()).orElseThrow(() -> new RecursoNaoEncontradoException("Etapa", req.getEtapaId()));
        Progresso p = new Progresso();
        p.setCidadaoId(cidadaoId);
        p.setEtapaId(req.getEtapaId());
        p.setStatus(req.getStatus());
        return mapper.map(dao.salvar(p), ProgressoResponse.class);
    }
}
