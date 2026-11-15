# LarOuAyê API (Java) — Domain Driven Design Using Java

API REST do **LarOuAyê**, "Seu primeiro imóvel, sem juridiquês". É o ponto central do sistema:
o React fala só com ela, ela fala com o Oracle e, quando precisa, com o serviço de IA (Flask).

> **A IA identifica. O Java decide. O conteúdo jurídico fundamenta.**

## Stack
Java 21 · Spring Boot 3.5 · **JDBC puro** (PreparedStatement, sem JPA/Hibernate/Spring Data) · Oracle · ModelMapper · Bean Validation · Docker multi-stage · Render.

## Como rodar
```bash
# 1) Banco: rode no Oracle `sql/00_executar_tudo.sql` (cópia dos scripts de ../larouaye-banco; recria o banco do zero)
# 2) Variáveis de ambiente (veja .env.example). Exemplo no terminal:
export DB_URL=jdbc:oracle:thin:@oracle.fiap.com.br:1521:ORCL
export DB_USER=RM000000 DB_PASSWORD=sua-senha
export CORS_ORIGINS=http://localhost:5173
export CLASSIFICADOR_URL=http://localhost:5000     # Flask da IA (opcional: há plano B)
# 3) Subir
mvn spring-boot:run
# 4) Testar
curl http://localhost:8080/api/saude
mvn test                                            # testes de regra de negócio (sem banco)
```
Teste completo no Postman: importe `postman/LarOuAye-API.postman_collection.json` e rode a coleção na ordem.

## Camadas (um sentido só: controller → service → dao)
```
controller/    recebe HTTP, valida (@Valid), devolve status certo. Sem regra de negócio.
service/       regras de negócio e decisão. Não conhece HTTP nem SQL.
dao/           interface + implementação JDBC. Só SQL. DataSource entra pelo construtor.
model/         entidades do domínio (espelham as tabelas).
dto/           contratos de entrada (Request, com validação) e de saída (Response).
exception/     exceções do domínio (404, 409, 400, 500).
handler/       @RestControllerAdvice: todo erro vira o mesmo JSON, sem stack trace.
classificador/ porta para a IA + Flask + plano B por regras.
config/        CORS, ModelMapper, cliente HTTP do Flask.
```

## Endpoints
| Recurso | Rotas | Observação |
|---|---|---|
| Cidadão | `POST/GET /api/usuarios`, `GET/PUT/DELETE /api/usuarios/{id}` | `/api/cidadaos` é o mesmo recurso (nome canônico) |
| Consulta (1:N) | `POST/GET /api/usuarios/{id}/consultas`, `GET/PUT/DELETE …/{consultaId}` | `POST` é o fluxo principal: classifica, decide, grava |
| Progresso (N:N) | `GET/POST /api/usuarios/{id}/etapas` | `POST` grava ou atualiza (idempotente) |
| Categoria | CRUD `/api/categorias` + `GET /api/categorias/{id}/orientacoes` | |
| Orientação | CRUD `/api/orientacoes` | conteúdo jurídico com fontes |
| Etapa | CRUD `/api/etapas` + `GET /api/etapas/{id}/documentos` | |
| Serviços | `GET /api/servicos?uf=SP` | canais oficiais por estado |
| Saúde | `GET /api/saude` | 200 se o banco responde, 503 se não |

Status usados: **200** leitura/atualização · **201 + Location** criação · **204** remoção · **400** dado inválido · **404** não existe · **405** método errado · **409** regra de negócio ou duplicidade · **415** tipo de conteúdo · **500** inesperado (sem detalhe interno).

Formato de erro (sempre o mesmo):
```json
{ "timestamp": "2026-11-15T10:00:00", "status": 400, "erro": "Bad Request",
  "mensagem": "Escreva sua dúvida.", "caminho": "/api/usuarios/1/consultas",
  "campos": { "pergunta": "Escreva sua dúvida." } }
```

## Regras de negócio (resumo)
1. **Decisão da consulta** (`ConsultaService`): confiança ≥ 0,70 e existe orientação ativa → `RESPONDIDA`; entre 0,30 e 0,70 → `ESCLARECIMENTO` (devolve opções); abaixo disso, fora do escopo ou categoria sem orientação → `NAO_RECONHECIDA`. Toda consulta é gravada.
2. **Orientação `REVISADO`** exige data de revisão e pelo menos uma fonte legal (409/400 se faltar).
3. **Plano B**: se o Flask cair ou demorar mais que o tempo limite, classifica por palavras-chave e a API continua respondendo.
4. **Anticorrupção**: o modelo usa 10 rótulos, o banco tem 6 categorias (`MapaCategorias`). Rótulo sem orientação revisada (ex.: leilão) **não inventa resposta**: encaminha.
5. Unicidade (e-mail, par canal+id externo, código de categoria/etapa, ordem da etapa) e **proteção contra remoção** com vínculos → 409.

## Deploy no Render
1. Suba este projeto para um repositório. No Render: **New → Web Service → Docker** (ou **Blueprint** com `render.yaml`).
2. Em *Environment*, cadastre `DB_URL`, `DB_USER`, `DB_PASSWORD`, `CORS_ORIGINS` (inclua a URL da Vercel) e `CLASSIFICADOR_URL`.
3. *Health Check Path*: `/api/saude`.
4. Na Vercel, aponte o rewrite `/api/:path*` para a URL do Render (já previsto no `vercel.json` do front).

## Verificação feita e limites (leia antes de entregar)
- Compilado com `javac` (Java 21) contra stubs das APIs do Spring/ModelMapper e **46 testes de regra passam** com DAOs falsos em memória.
- **Ainda não foi feito** `mvn package` real, nem execução contra Oracle e Flask reais, nem a coleção Postman rodada. O ambiente onde foi escrito não alcança o Maven Central. **Antes de entregar, rode `mvn clean package` e a coleção Postman na máquina do grupo.**
- Todos os nomes de tabela/coluna/sequence usados nos DAOs foram conferidos contra `01_ddl.sql`.
- O Oracle da FIAP pode não aceitar conexões vindas do Render. Se não aceitar, use outro Oracle acessível (ex.: Oracle Cloud gratuito) e troque `DB_URL`.
- O conteúdo jurídico semeado está `PENDENTE` de revisão por advogado.
