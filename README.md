

\# LarOuAyê



\### Seu primeiro imóvel, sem juridiquês.



Global Solution 2026/2 — CitizenTech  

Domain Driven Design Using Java  

FIAP | 1TDSPY | TM Consulting



\## 1. Sobre o projeto



O LarOuAyê é uma solução CitizenTech que busca democratizar

o acesso à informação jurídica relacionada à aquisição do

primeiro imóvel, oferecendo orientações em linguagem acessível,

consultas jurídicas informativas e acompanhamento das etapas

da jornada imobiliária.



\## 2. Integrantes



| Nome | RM |

|---|---|

| Davi Felix Cunha | 569393 |

| Fernando Oliveira Francelino Sardinha | 570196 |

| Jonatan Vieira Feitosa | 570452 |

| Marcelo Candido da Mata Junior | 569584 |

| Vinicius Liberato dos Anjos | 571480 |



\## 3. Links da entrega



\- API: https://larouaye-api-fzqg.onrender.com

\- Health Check: https://larouaye-api-fzqg.onrender.com/api/saude

\- Repositório: https://github.com/Jonatanfeitosa/larouaye-java

\- Front-end: \[INSERIR LINK DEFINITIVO]

\- Vídeo demonstrativo: \[INSERIR LINK DO YOUTUBE NÃO LISTADO]



\## 4. Tecnologias utilizadas



\- Java 21

\- Spring Boot

\- Maven

\- JDBC

\- Oracle Database

\- ModelMapper

\- Bean Validation

\- JUnit e Mockito

\- Docker

\- Render

\- React e TypeScript



\## 5. Arquitetura



Front-end React → API REST Spring Boot → JDBC → Oracle FIAP



O backend está organizado em pacotes com responsabilidades

específicas, utilizando DAOs, DTOs, controllers, serviços,

exceções próprias e tratamento global de erros.



\## 6. Execução local



\### Pré-requisitos



\- Java 21

\- Maven

\- Git

\- Acesso ao Oracle da FIAP



\### Clonar o projeto



```bash

git clone https://github.com/Jonatanfeitosa/larouaye-java.git

cd larouaye-java

```



\### Configurar o banco de dados



O script SQL consolidado está disponível em:



`sql/ENTREGA\_LarOuAye\_Banco.sql`



O script contém a estrutura relacional definitiva, composta

por 17 tabelas, 13 sequences e dados demonstrativos.



Execute o script somente em um schema destinado à instalação

inicial, caso o banco ainda não esteja configurado.



\### Configurar as variáveis de ambiente



No Windows PowerShell:



```powershell

$env:DB\_URL = "jdbc:oracle:thin:@oracle.fiap.com.br:1521:ORCL"

$env:DB\_USER = "SEU\_USUARIO"

$env:DB\_PASSWORD = "SUA\_SENHA"

$env:CORS\_ORIGINS = "http://localhost:5173"

```



As credenciais devem ser fornecidas exclusivamente por

variáveis de ambiente e nunca versionadas no GitHub.



\### Executar a aplicação



```bash

mvn spring-boot:run

```



API local: http://localhost:8080



\### Executar os testes automatizados



```bash

mvn clean test

```



\## 7. Principais recursos da API



| Recurso | Endpoint |

|---|---|

| Saúde da aplicação | /api/saude |

| Cidadãos | /api/usuarios |

| Categorias | /api/categorias |

| Orientações jurídicas | /api/orientacoes |

| Etapas da jornada | /api/etapas |

| Serviços | /api/servicos |

| Progresso do cidadão | /api/usuarios/{id}/etapas |

| Consultas jurídicas | /api/usuarios/{id}/consultas |



\## 8. Publicação



A API está publicada no Render por meio de um Dockerfile

multi-stage, conectada ao Oracle da FIAP.



O plano gratuito pode apresentar demora na primeira

requisição após um período de inatividade.



\## 9. Documentação técnica



A documentação técnica completa integra o arquivo PDF

entregue no Portal do Aluno, contemplando diagramas,

modelagem relacional, endpoints, regras de negócio,

validações e configuração do ambiente.



\---



FIAP — Global Solution 2026/2  

Projeto acadêmico desenvolvido pela equipe TM Consulting.



