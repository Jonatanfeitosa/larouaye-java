# Roteiro do vídeo — Java (máx. 5 min)

Grave a tela com o front publicado, o Postman e o código aberto. Cada pessoa fala a parte que desenvolveu.

| Tempo | Cena | Fala sugerida |
|---|---|---|
| 0:00–0:20 | Slide/README | "Esta é a API do LarOuAyê. Ela é o centro do sistema: o React fala só com ela." Mostre a frase: *a IA identifica, o Java decide*. |
| 0:20–0:50 | Estrutura de pastas | Camadas: controller → service → dao. "Cada camada tem uma responsabilidade." |
| 0:50–1:40 | `JdbcOrientacaoDao` | `PreparedStatement`, `DataSource` pelo construtor, transação com commit/rollback, sem JPA. Mostre `01_ddl.sql` com `tb_`, `cd_`, sequences. |
| 1:40–2:20 | DTOs e validação | `ConsultaRequest` com `@NotBlank/@Size`, `@Valid` no controller, ModelMapper. |
| 2:20–3:20 | Postman | `POST /usuarios` → 201 e **Location**; `POST` repetido → 409; JSON inválido → 400; `PATCH` → 405; id inexistente → 404. Mostre que o erro não tem stack trace. |
| 3:20–4:10 | Front publicado | Faça uma pergunta no Assistente. Mostre a resposta com fonte legal e, no Oracle, a linha nova em `tb_consulta`. |
| 4:10–4:40 | Regra de decisão | `ConsultaService`: limiares 0,70 e 0,30; pergunta fora do tema → NAO_RECONHECIDA. Derrube o Flask e repita: o plano B responde. |
| 4:40–5:00 | Deploy | Render com variáveis de ambiente e `/api/saude`. "Obrigado." |

**Dica:** ensaie uma vez com cronômetro. Se passar de 5 minutos, corte a cena do DAO para 30 s.
