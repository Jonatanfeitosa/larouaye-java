# Checklist da rubrica (Domain Driven Design Using Java) → onde está a evidência

| Item (pontos) | O que o edital pede | Onde está |
|---|---|---|
| Modelagem (5) | ≥ 3 entidades, 1:N, pacotes `model, dao, dto, controller, exception, handler, config` | `model/` (Cidadao, Categoria, Orientacao, Etapa, Consulta, Progresso, Servico…), 1:N Cidadão→Consulta, Categoria→Orientacao, Etapa→Documento |
| JDBC (15) | DAO por entidade, CRUD, `PreparedStatement`, `DataSource` no construtor, scripts Oracle (`tb_`, `cd_`, `nm_`, `ds_`, `dt_`, sequences), sem JPA | `dao/Jdbc*Dao` (7), `sql` em `sql/01_ddl.sql`; transações manuais em Orientacao/Etapa/Progresso |
| DTOs (10) | DTO de entrada e saída, ModelMapper, Bean Validation, `@Valid` | `dto/*Request` (validação) e `*Response`; `config/ModelMapperConfig`; `@Valid` em todo `@RequestBody` |
| Controllers (10) | REST, rotas aninhadas 1:N, 200/201+Location/204 | `controller/*`; aninhadas: `/usuarios/{id}/consultas`, `/usuarios/{id}/etapas`, `/categorias/{id}/orientacoes`, `/etapas/{id}/documentos` |
| Exceções (10) | exceções próprias, `@RestControllerAdvice`, 400/404/405/409, sem stack trace | `exception/*`, `handler/GlobalExceptionHandler`; `application.yml` desliga stacktrace |
| Integração (5) | front real via fetch; Flask quando disponível, funcionando se falhar | `classificador/*` (Flask + plano B); CORS em `config/CorsConfig`; front `services/` |
| Render (5) | Dockerfile multi-stage, variáveis de ambiente | `Dockerfile`, `render.yaml`, `.env.example` |
| Documentação (10) | PDF técnico | `docs/DOCUMENTACAO-TECNICA.md` (exportar para PDF) |
| Apresentação (30) | vídeo ≤ 5 min, Java integrado ao front | `docs/ROTEIRO-VIDEO.md` |

## Teste de aceite (o que o professor vai pedir)
- [ ] CRUD de cada entidade no Postman (coleção pronta, 50 requisições)
- [ ] Erro em JSON sem stack trace (400, 404, 405, 409)
- [ ] Oracle persiste (mostrar `SELECT` após o `POST`)
- [ ] API remota no Render responde `/api/saude`
- [ ] Front publicado faz `GET` e `POST` reais
- [ ] Derrubar o Flask e mostrar que a API ainda responde (plano B)
- [ ] Não existe `import javax.persistence`, `org.hibernate` nem `spring-data` no projeto (`grep -r` mostra vazio)
