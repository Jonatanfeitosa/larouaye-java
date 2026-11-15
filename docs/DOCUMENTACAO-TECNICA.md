# LarOuAyê — Documentação Técnica da API Java
**Disciplina:** Domain Driven Design Using Java · **Global Solution FIAP 2026/2 — CitizenTech**
**Integrantes:** Davi Felix Cunha · Fernando Oliveira Francelino (RM 570196) · Jonatan Feitosa · Marcelo da Mata · Vinícius Liberato *(preencher RMs)*

## 1. Problema e objetivo
Quem compra o primeiro imóvel enfrenta linguagem jurídica difícil e muitos riscos (dívidas no imóvel, contratos abusivos, golpes). O LarOuAyê é uma plataforma gratuita que explica cada etapa da compra em linguagem simples, com fonte legal. Esta API é o núcleo: recebe a dúvida, decide a melhor orientação, registra o histórico e entrega o conteúdo ao front.
Alinhamento: ODS 10 (redução das desigualdades) e ODS 11 (cidades e comunidades sustentáveis).

## 2. Princípio de projeto
> **A IA identifica. O Java decide. O conteúdo jurídico fundamenta.**

Um classificador com confiança alta não prova que a resposta jurídica está certa. Por isso a IA só **sugere** a categoria; quem decide é uma regra escrita em Java, e o texto vem de um catálogo jurídico com fonte e data de revisão guardado no Oracle.

## 3. Arquitetura
```
React (Vercel) --fetch/JSON--> API Java (Render) --JDBC--> Oracle
                                   |
                                   +--HTTP (timeout 3 s)--> Flask (2 modelos de ML)
                                   |      se falhar: plano B por palavras-chave
```
Camadas, em um sentido só: `controller → service → dao`. O controller não tem regra de negócio; o service não conhece HTTP nem SQL; o DAO só tem SQL.

### 3.1 Pacotes
`model` · `dao` · `dto` · `controller` · `service` · `exception` · `handler` · `config` · `classificador`.

## 4. Modelo de dados (resumo)
11 tabelas Oracle (`tb_cidadao, tb_categoria, tb_orientacao, tb_cuidado, tb_fonte_legal, tb_etapa, tb_etapa_documento, tb_etapa_orientacao, tb_cidadao_etapa, tb_consulta, tb_servico`), sequences `sq_*`, PK/FK/UNIQUE/CHECK e comentários.
- **1:N**: Cidadão→Consulta · Categoria→Orientacao · Orientacao→Cuidado/FonteLegal · Etapa→Documento.
- **N:N** com tabela associativa: Cidadão×Etapa (com status) e Etapa×Orientação.
Convenção de nomes: `tb_` tabela, `cd_` código/chave, `nm_` nome, `ds_` descrição, `dt_` data, `sg_` sigla, `nr_` número, `vl_` valor, `st_` situação.

## 5. Persistência com JDBC
- Uma interface e uma implementação `Jdbc*Dao` por entidade; **sem JPA, Hibernate ou Spring Data**.
- `DataSource` recebido **pelo construtor**; `PreparedStatement` em todas as consultas (sem concatenar valores: protege contra SQL injection).
- Chaves geradas por sequence (`sq_x.NEXTVAL`) e lidas com `getGeneratedKeys` pelo nome da coluna.
- **Transações manuais** (`commit/rollback`) nos agregados: orientação (com cuidados e fontes), etapa (com documentos) e progresso (UPDATE e, se preciso, INSERT).
- Carga dos filhos em **uma consulta por lote** (`IN (...)`, até 500 itens) para evitar o problema N+1.
- Erros do Oracle traduzidos para o domínio: ORA-00001 → 409 duplicado; ORA-02292 → 409 tem filhos; ORA-02291 → 409 pai inexistente; ORA-02290 → 409 CHECK; demais → 500 sem detalhe.

## 6. DTOs, validação e mapeamento
DTOs de entrada (`*Request`) trazem Bean Validation (`@NotBlank`, `@Size`, `@Pattern`, `@Email`, `@Positive`, `@PastOrPresent`); todo `@RequestBody` usa `@Valid`. DTOs de saída (`*Response`) escondem detalhes internos. ModelMapper em modo `STRICT` converte campos simples; listas e padrões (ex.: status `PENDENTE`) são tratados no service para ficar explícito.

## 7. Tratamento de erros
`GlobalExceptionHandler` (`@RestControllerAdvice`) transforma qualquer falha no mesmo JSON (`timestamp, status, erro, mensagem, caminho, campos`). O detalhe técnico vai só para o log. `application.yml` desliga stack trace e mensagem padrão do Spring.

| Status | Quando |
|---|---|
| 200 | leitura e atualização |
| 201 + Location | criação |
| 204 | remoção |
| 400 | validação, JSON quebrado, UF/etapa inválida |
| 404 | recurso inexistente |
| 405 | método não permitido |
| 409 | duplicidade ou regra de negócio |
| 415 | corpo que não é JSON |
| 500 | falha inesperada (mensagem genérica) |

## 8. Endpoints
Ver tabela no `README.md` e a coleção `postman/LarOuAye-API.postman_collection.json` (50 requisições com testes automáticos de status, Location e ausência de stack trace).
Rotas aninhadas 1:N: `/usuarios/{id}/consultas`, `/usuarios/{id}/etapas`, `/categorias/{id}/orientacoes`, `/etapas/{id}/documentos`.

## 9. Regras de negócio
1. **Decisão da consulta**: confiança ≥ 0,70 e orientação ativa → `RESPONDIDA`; 0,30 a 0,70 → `ESCLARECIMENTO` (até 3 opções); abaixo, fora do escopo ou sem orientação → `NAO_RECONHECIDA`. Limiares configuráveis por variável de ambiente.
2. Dentro da categoria, escolhe-se a orientação que mais compartilha palavras com a pergunta.
3. **Orientação `REVISADO`** exige data de revisão e ao menos uma fonte legal.
4. **Camada anticorrupção** `MapaCategorias`: 10 rótulos do modelo → 6 categorias do banco. Rótulos sem orientação revisada (leilão, fraude) não geram resposta inventada.
5. Unicidade de e-mail, de (canal, id externo), de código (categoria/etapa) e de ordem de etapa.
6. Não remover: cidadão com histórico, categoria com orientações, etapa com progresso, orientação já entregue (desative com `ativo=false`).
7. Toda consulta é registrada, inclusive as não reconhecidas (histórico e melhoria do modelo).

## 10. Integração com a IA (Flask)
`ClassificadorPort` é a porta de saída; `ClassificadorComFallback` tenta o Flask (`RestClient`, timeout configurável) e, em qualquer falha, usa `ClassificadorPorRegras` (palavras-chave, confiança limitada a 0,85). A API **não depende** da IA para funcionar.

## 11. Segurança e privacidade
Sem dados sensíveis (sem CPF): só nome, e-mail opcional e canal. CORS restrito às origens da variável `CORS_ORIGINS`. Segredos só em variáveis de ambiente. Usuário não-root no contêiner. Respostas jurídicas informam que a orientação é geral e não substitui advogado.

## 12. Testes
- **Unidade (JUnit 5, sem banco)**: 46 testes das regras dos serviços com DAOs falsos em memória (decisão, revisão, unicidade, 404, remoções protegidas, plano B).
- **Integração/API**: coleção Postman rodando contra Oracle real.
- Verificação estática: nomes de tabela/coluna/sequence dos DAOs conferidos contra o DDL.

## 13. Deploy
Dockerfile multi-stage (build com Maven, execução em JRE Alpine, usuário sem privilégios) publicado no Render com variáveis de ambiente e health check `/api/saude`. O front na Vercel usa rewrite `/api/:path*` para a URL do Render.

## 14. Limitações e próximos passos
- Conteúdo jurídico semeado ainda precisa de revisão por advogado.
- Histórico do Telegram não passa pelo Java nesta versão (o bot fala com o Flask).
- Sem autenticação (público-alvo e escopo da entrega); evolução: login e limite de requisições.
- Leilão e fraude ainda sem orientação revisada: o sistema encaminha em vez de responder.
