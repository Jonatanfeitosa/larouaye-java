package br.com.fiap.larouaye.service;

import br.com.fiap.larouaye.dao.ServicoDao;
import br.com.fiap.larouaye.dto.ServicoResponse;
import br.com.fiap.larouaye.exception.DadosInvalidosException;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/** Guia de serviços: canais oficiais por UF (links, sem prometer lista completa). */
@Service
public class ServicoService {

    private final ServicoDao dao;
    private final ModelMapper mapper;

    public ServicoService(ServicoDao dao, ModelMapper mapper) {
        this.dao = dao;
        this.mapper = mapper;
    }

    public List<ServicoResponse> listarPorUf(String uf) {
        String sigla = Ufs.normalizar(uf);
        if (!Ufs.valida(sigla)) {
            throw new DadosInvalidosException("UF inválida. Use a sigla do estado, por exemplo SP.");
        }
        return dao.listarPorUf(sigla).stream().map(s -> mapper.map(s, ServicoResponse.class)).toList();
    }
}
