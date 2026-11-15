package br.com.fiap.larouaye.fake;

import br.com.fiap.larouaye.dao.CategoriaDao;
import br.com.fiap.larouaye.dao.CidadaoDao;
import br.com.fiap.larouaye.dao.ConsultaDao;
import br.com.fiap.larouaye.dao.EtapaDao;
import br.com.fiap.larouaye.dao.OrientacaoDao;
import br.com.fiap.larouaye.dao.ProgressoDao;
import br.com.fiap.larouaye.model.Categoria;
import br.com.fiap.larouaye.model.Cidadao;
import br.com.fiap.larouaye.model.Consulta;
import br.com.fiap.larouaye.model.Etapa;
import br.com.fiap.larouaye.model.EtapaDocumento;
import br.com.fiap.larouaye.model.Orientacao;
import br.com.fiap.larouaye.model.Progresso;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/**
 * DAOs falsos em memória. Servem para testar as REGRAS DE NEGÓCIO dos serviços sem Oracle
 * (teste de unidade: rápido, sem rede, sem banco). Os DAOs reais (JDBC) são testados no Oracle/Postman.
 */
public final class FakeDaos {

    private FakeDaos() {
    }

    /** Estado compartilhado: assim "possuiFilhos" e "possuiConsultas" enxergam os outros fakes. */
    public static class Banco {
        public final Map<Long, Categoria> categorias = new LinkedHashMap<>();
        public final Map<Long, Orientacao> orientacoes = new LinkedHashMap<>();
        public final Map<Long, Etapa> etapas = new LinkedHashMap<>();
        public final Map<Long, Cidadao> cidadaos = new LinkedHashMap<>();
        public final Map<Long, Consulta> consultas = new LinkedHashMap<>();
        public final Map<String, Progresso> progressos = new LinkedHashMap<>();
        public final AtomicLong seq = new AtomicLong(1000);

        public final CategoriaDao categoriaDao = new FakeCategoriaDao(this);
        public final OrientacaoDao orientacaoDao = new FakeOrientacaoDao(this);
        public final EtapaDao etapaDao = new FakeEtapaDao(this);
        public final CidadaoDao cidadaoDao = new FakeCidadaoDao(this);
        public final ConsultaDao consultaDao = new FakeConsultaDao(this);
        public final ProgressoDao progressoDao = new FakeProgressoDao(this);
    }

    static class FakeCategoriaDao implements CategoriaDao {
        private final Banco b;

        FakeCategoriaDao(Banco b) {
            this.b = b;
        }

        public Categoria inserir(Categoria c) {
            c.setId(b.seq.incrementAndGet());
            b.categorias.put(c.getId(), c);
            return c;
        }

        public List<Categoria> listar() {
            return new ArrayList<>(b.categorias.values());
        }

        public Optional<Categoria> buscarPorId(Long id) {
            return Optional.ofNullable(b.categorias.get(id));
        }

        public Optional<Categoria> buscarPorCodigo(String codigo) {
            return b.categorias.values().stream().filter(c -> c.getCodigo().equals(codigo)).findFirst();
        }

        public boolean existeCodigo(String codigo, Long ignorarId) {
            return b.categorias.values().stream().anyMatch(c -> c.getCodigo().equals(codigo) && !c.getId().equals(ignorarId));
        }

        public boolean atualizar(Categoria c) {
            if (!b.categorias.containsKey(c.getId())) {
                return false;
            }
            b.categorias.put(c.getId(), c);
            return true;
        }

        public boolean remover(Long id) {
            return b.categorias.remove(id) != null;
        }

        public boolean possuiFilhos(Long id) {
            return b.orientacoes.values().stream().anyMatch(o -> o.getCategoriaId().equals(id))
                    || b.consultas.values().stream().anyMatch(q -> id.equals(q.getCategoriaId()));
        }
    }

    static class FakeOrientacaoDao implements OrientacaoDao {
        private final Banco b;

        FakeOrientacaoDao(Banco b) {
            this.b = b;
        }

        private Orientacao completar(Orientacao o) {
            Categoria c = b.categorias.get(o.getCategoriaId());
            o.setCategoriaCodigo(c == null ? null : c.getCodigo());
            return o;
        }

        public Orientacao inserir(Orientacao o) {
            o.setId(b.seq.incrementAndGet());
            b.orientacoes.put(o.getId(), completar(o));
            return o;
        }

        public List<Orientacao> listar() {
            return new ArrayList<>(b.orientacoes.values());
        }

        public List<Orientacao> listarPorCategoria(Long categoriaId) {
            return b.orientacoes.values().stream().filter(o -> o.getCategoriaId().equals(categoriaId)).toList();
        }

        public List<Orientacao> listarAtivasPorCategorias(Collection<Long> ids) {
            return b.orientacoes.values().stream().filter(o -> o.isAtivo() && ids.contains(o.getCategoriaId())).toList();
        }

        public List<Orientacao> listarPorEtapa(Long etapaId) {
            return List.of();
        }

        public Optional<Orientacao> buscarPorId(Long id) {
            return Optional.ofNullable(b.orientacoes.get(id));
        }

        public boolean atualizar(Orientacao o) {
            if (!b.orientacoes.containsKey(o.getId())) {
                return false;
            }
            b.orientacoes.put(o.getId(), completar(o));
            return true;
        }

        public boolean remover(Long id) {
            return b.orientacoes.remove(id) != null;
        }

        public boolean possuiConsultas(Long id) {
            return b.consultas.values().stream().anyMatch(q -> id.equals(q.getOrientacaoId()));
        }
    }

    static class FakeEtapaDao implements EtapaDao {
        private final Banco b;

        FakeEtapaDao(Banco b) {
            this.b = b;
        }

        public Etapa inserir(Etapa e) {
            e.setId(b.seq.incrementAndGet());
            b.etapas.put(e.getId(), e);
            return e;
        }

        public List<Etapa> listar() {
            return b.etapas.values().stream().sorted(Comparator.comparingInt(Etapa::getOrdem)).toList();
        }

        public Optional<Etapa> buscarPorId(Long id) {
            return Optional.ofNullable(b.etapas.get(id));
        }

        public Optional<Etapa> buscarPorCodigo(String codigo) {
            return b.etapas.values().stream().filter(e -> e.getCodigo().equals(codigo)).findFirst();
        }

        public boolean existeCodigo(String codigo, Long ignorarId) {
            return b.etapas.values().stream().anyMatch(e -> e.getCodigo().equals(codigo) && !e.getId().equals(ignorarId));
        }

        public boolean existeOrdem(int ordem, Long ignorarId) {
            return b.etapas.values().stream().anyMatch(e -> e.getOrdem() == ordem && !e.getId().equals(ignorarId));
        }

        public boolean atualizar(Etapa e) {
            if (!b.etapas.containsKey(e.getId())) {
                return false;
            }
            b.etapas.put(e.getId(), e);
            return true;
        }

        public boolean remover(Long id) {
            return b.etapas.remove(id) != null;
        }

        public boolean possuiProgresso(Long id) {
            return b.progressos.values().stream().anyMatch(p -> p.getEtapaId().equals(id));
        }

        public List<EtapaDocumento> listarDocumentos(Long etapaId) {
            Etapa e = b.etapas.get(etapaId);
            return e == null ? List.of() : e.getDocumentos();
        }
    }

    static class FakeCidadaoDao implements CidadaoDao {
        private final Banco b;

        FakeCidadaoDao(Banco b) {
            this.b = b;
        }

        public Cidadao inserir(Cidadao c) {
            c.setId(b.seq.incrementAndGet());
            c.setDataCadastro(LocalDateTime.now());
            b.cidadaos.put(c.getId(), c);
            return c;
        }

        public List<Cidadao> listar() {
            return new ArrayList<>(b.cidadaos.values());
        }

        public Optional<Cidadao> buscarPorId(Long id) {
            return Optional.ofNullable(b.cidadaos.get(id));
        }

        public boolean existeEmail(String email, Long ignorarId) {
            return b.cidadaos.values().stream().anyMatch(c -> email.equalsIgnoreCase(c.getEmail()) && !c.getId().equals(ignorarId));
        }

        public boolean existeCanalExterno(String canal, String idExterno, Long ignorarId) {
            return b.cidadaos.values().stream().anyMatch(c -> c.getCanal().name().equals(canal)
                    && idExterno.equals(c.getIdExterno()) && !c.getId().equals(ignorarId));
        }

        public boolean atualizar(Cidadao c) {
            if (!b.cidadaos.containsKey(c.getId())) {
                return false;
            }
            c.setDataCadastro(b.cidadaos.get(c.getId()).getDataCadastro());
            b.cidadaos.put(c.getId(), c);
            return true;
        }

        public boolean remover(Long id) {
            return b.cidadaos.remove(id) != null;
        }

        public boolean possuiRegistros(Long id) {
            return b.consultas.values().stream().anyMatch(q -> q.getCidadaoId().equals(id))
                    || b.progressos.values().stream().anyMatch(p -> p.getCidadaoId().equals(id));
        }
    }

    static class FakeConsultaDao implements ConsultaDao {
        private final Banco b;

        FakeConsultaDao(Banco b) {
            this.b = b;
        }

        private Consulta completar(Consulta q) {
            Categoria c = q.getCategoriaId() == null ? null : b.categorias.get(q.getCategoriaId());
            q.setCategoriaCodigo(c == null ? null : c.getCodigo());
            return q;
        }

        public Consulta inserir(Consulta q) {
            q.setId(b.seq.incrementAndGet());
            q.setDataConsulta(LocalDateTime.now());
            b.consultas.put(q.getId(), completar(q));
            return q;
        }

        public List<Consulta> listarPorCidadao(Long cidadaoId) {
            return b.consultas.values().stream().filter(q -> q.getCidadaoId().equals(cidadaoId)).toList();
        }

        public Optional<Consulta> buscarPorId(Long id) {
            return Optional.ofNullable(b.consultas.get(id));
        }

        public boolean atualizar(Consulta q) {
            if (!b.consultas.containsKey(q.getId())) {
                return false;
            }
            b.consultas.put(q.getId(), completar(q));
            return true;
        }

        public boolean remover(Long id) {
            return b.consultas.remove(id) != null;
        }
    }

    static class FakeProgressoDao implements ProgressoDao {
        private final Banco b;

        FakeProgressoDao(Banco b) {
            this.b = b;
        }

        public List<Progresso> listarPorCidadao(Long cidadaoId) {
            return b.progressos.values().stream().filter(p -> p.getCidadaoId().equals(cidadaoId)).toList();
        }

        public Progresso salvar(Progresso p) {
            p.setAtualizadoEm(LocalDateTime.now());
            b.progressos.put(p.getCidadaoId() + ":" + p.getEtapaId(), p);
            return p;
        }
    }
}
