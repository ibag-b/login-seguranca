# MS-AV-02 — Segurança e Observabilidade

Sistema de login RESTful em Spring Boot, com documentação OpenAPI/Swagger,
indicadores no Actuator/Prometheus e segurança por *roles* usando
`JdbcUserDetailsManager`.

## Como executar

```
mvnw spring-boot:run
```

A aplicação sobe em `http://localhost:8080` com banco H2 em memória, criado e
populado pelo `src/main/resources/data.sql`.

## Endpoints

| Método | Rota | Acesso | Descrição |
|---|---|---|---|
| POST | `/login` | público | Recebe `{"username": "", "senha": ""}` e devolve **200** (válido) ou **401** (inválido ou bloqueado) |
| PUT | `/trocarsenha/{userid}` | público | Recebe `{"atual": "", "nova": ""}` e troca a senha se a atual conferir |
| POST | `/` | `ROLE_MANUTENCAO` | Cadastra um usuário a partir de `{"username": "", "senha": ""}` |
| PUT | `/unlock/{userid}` | `ROLE_ADMIN` | Desbloqueia um usuário bloqueado por excesso de tentativas |
| GET | `/` | `ROLE_MANUTENCAO` | Lista todos os usuários cadastrados em JSON |

Documentação dos endpoints (OpenAPI/Swagger):

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- JSON OpenAPI: `http://localhost:8080/v3/api-docs`

## Regra de bloqueio

Cada falha de login é registrada na tabela `login_attempts`. Quando o usuário
falha **mais de uma vez**, a coluna `locked` passa a `TRUE` e novas tentativas
devolvem 401 mesmo com a senha correta, até um administrador chamar
`PUT /unlock/{userid}`. Um login bem-sucedido zera o contador de falhas.

O limite é configurável em `application.properties` por
`seguranca.max-tentativas` (padrão: 2).

## Usuários

Usuários da API (tabelas `users` / `authorities` do Spring Security, lidas pelo
`JdbcUserDetailsManager`, autenticação HTTP Basic):

| Usuário | Senha | Role |
|---|---|---|
| `manutencao` | `manutencao123` | `ROLE_MANUTENCAO` |
| `admin` | `admin123` | `ROLE_ADMIN` |

Usuários do sistema de login (tabela `usuarios` do enunciado):

| Usuário | Senha |
|---|---|
| `maria` | `maria123` |
| `joao` | `joao123` |
| `ana` | `ana123` |

## Indicadores (observabilidade)

| Métrica (Micrometer) | Nome no Prometheus | O que mede |
|---|---|---|
| `logins.validos` | `logins_validos_total` | Quantidade de logins válidos |
| `logins.invalidos` | `logins_invalidos_total` | Quantidade de logins inválidos |
| `usuarios.bloqueados` | `usuarios_bloqueados` | Total de usuários bloqueados no momento |

Endpoints do Actuator:

- `http://localhost:8080/actuator/health`
- `http://localhost:8080/actuator/metrics/logins.validos`
- `http://localhost:8080/actuator/prometheus`

## Prometheus e Grafana

1. Subir o Prometheus apontando para `monitoring/prometheus.yml`:
   ```
   docker run -p 9090:9090 -v "%cd%/monitoring/prometheus.yml:/etc/prometheus/prometheus.yml" prom/prometheus
   ```
2. Subir o Grafana e acessar `http://localhost:3000` (`admin`/`admin`).
3. Adicionar o Prometheus como fonte de dados (`http://host.docker.internal:9090`).
4. Importar o dashboard `monitoring/grafana-dashboard.json`
   (*Dashboards → New → Import → Upload JSON file*).

O print do dashboard está em `monitoring/dashboard.png` e o dashboard exportado
como código está em `monitoring/grafana-dashboard.json`.

## Estrutura

```
src/main/java/aula/microservicos/loginseguranca/
├── LoginSegurancaApplication.java      # @OpenAPIDefinition + @SecurityScheme
├── GerenciamentoException.java         # validação -> HTTP 400
├── usuarios/
│   ├── UsuarioController.java          # os 5 endpoints do enunciado
│   ├── UsuarioService.java             # regras de login, bloqueio e cadastro
│   ├── UsuarioRepository.java          # tabela usuarios (JdbcTemplate)
│   ├── LoginAttemptRepository.java     # tabela login_attempts (JdbcTemplate)
│   ├── Usuario.java / LoginRequest.java / TrocarSenhaRequest.java / NovoUsuarioRequest.java
├── seguranca/
│   └── SecurityConfig.java             # JdbcUserDetailsManager + regras por role
└── metricas/
    └── LoginMetricas.java              # os três indicadores
```
