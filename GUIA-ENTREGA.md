# Guia passo a passo — MS-AV-02

Este arquivo é só para você se guiar; ele pode ficar no repositório sem problema.

## Passo 1 — Abrir e rodar

1. Extraia o zip (cuidado com a pasta duplicada que o Windows cria: se virar
   `login-seguranca\login-seguranca\...`, entre na subpasta).
2. Abra a pasta no VS Code.
3. Abra um terminal **Prompt de Comando** (não PowerShell) e rode:
   ```
   mvnw spring-boot:run
   ```
4. Espere aparecer `Started LoginSegurancaApplication`.

## Passo 2 — Testar os endpoints (Thunder Client)

Ordem sugerida, que já exercita todas as regras do enunciado:

### 2.1 Login válido → 200
`POST http://localhost:8080/login` · Body → JSON:
```json
{ "username": "maria", "senha": "maria123" }
```

### 2.2 Login inválido (1ª falha) → 401
```json
{ "username": "joao", "senha": "errada" }
```

### 2.3 Login inválido de novo (2ª falha) → 401 **e o joao fica bloqueado**
Repita a mesma requisição acima. A partir daqui, mesmo com a senha certa
(`joao123`) o login devolve 401 com a mensagem de usuário bloqueado.

### 2.4 Trocar senha (público) → 200
`PUT http://localhost:8080/trocarsenha/maria` · Body:
```json
{ "atual": "maria123", "nova": "maria999" }
```
Com a senha atual errada, devolve 401.

### 2.5 Listar usuários sem login → 401
`GET http://localhost:8080/`

### 2.6 Listar usuários como manutenção → 200
Mesma requisição, mas na aba **Auth → Basic** do Thunder Client preencha
`manutencao` / `manutencao123`.

### 2.7 Cadastrar usuário (ROLE_MANUTENCAO) → 201
`POST http://localhost:8080/` com Auth Basic `manutencao`/`manutencao123` e Body:
```json
{ "username": "carlos", "senha": "carlos123" }
```

### 2.8 Desbloquear com o usuário errado → 403
`PUT http://localhost:8080/unlock/joao` com Auth Basic `manutencao`/`manutencao123`
(manutenção não tem ROLE_ADMIN).

### 2.9 Desbloquear como admin → 200
Mesma requisição com Auth Basic `admin`/`admin123`. Depois disso o `joao`
consegue logar de novo com `joao123`.

> Dica: deixe o `POST /login` com senha errada rodando algumas vezes e faça
> alguns logins válidos — assim os três indicadores ficam com números
> diferentes de zero e o dashboard do Grafana fica bonito no print.

## Passo 3 — Conferir a documentação (item 01 do enunciado)

Abra `http://localhost:8080/swagger-ui/index.html`. Todos os endpoints
aparecem documentados; o botão **Authorize** permite informar
`manutencao`/`manutencao123` ou `admin`/`admin123` para testar os protegidos
direto por lá.

## Passo 4 — Prometheus (item 02)

Com o Docker instalado, **dentro da pasta do projeto**:

```
docker run -p 9090:9090 -v "%cd%/monitoring/prometheus.yml:/etc/prometheus/prometheus.yml" prom/prometheus
```

Abra `http://localhost:9090`, vá em *Graph* e consulte:

- `logins_validos_total`
- `logins_invalidos_total`
- `usuarios_bloqueados`

Se as três aparecerem com valores, está coletando certo.

## Passo 5 — Grafana e o dashboard

```
docker run -p 3000:3000 grafana/grafana-oss
```

1. Abra `http://localhost:3000` e entre com `admin` / `admin`.
2. *Connections → Data sources → Add new data source → Prometheus*,
   URL `http://host.docker.internal:9090`, **Save & test**.
3. *Dashboards → New → Import → Upload dashboard JSON file* e escolha
   `monitoring/grafana-dashboard.json` deste projeto. Selecione o Prometheus
   que você acabou de criar e clique em **Import**.
4. O dashboard abre com os três indicadores pedidos no enunciado.

### Print screen (obrigatório na entrega)

Com o dashboard aberto e com números nele, tire um print da tela e salve como
`monitoring/dashboard.png` dentro da pasta do projeto.

### Exportar o dashboard como código (obrigatório na entrega)

No dashboard aberto: botão **Export** (ou *Share → Export*) →
**Export as JSON** / *Save to file*. Salve o arquivo baixado dentro de
`monitoring/` (pode manter o nome `grafana-dashboard.json`, substituindo o que
veio no projeto — assim o que está no repositório é exatamente o dashboard que
você montou).

## Passo 6 — Publicar no GitHub (repositório PÚBLICO)

No VS Code, aba **Source Control**:

1. **Initialize Repository**
2. Escreva a mensagem do commit (ex: `MS-AV-02 seguranca e observabilidade`) e
   clique em **✓ Commit**.
3. **Publish Branch → Publish to GitHub public repository** (tem que ser
   público, igual à atividade anterior).

Se aparecer o erro de repositório já existente, use o terminal:
```
git remote add origin https://github.com/SEU-USUARIO/NOME-DO-REPO.git
git branch -M main
git push -u origin main
```

Antes de entregar, confira no GitHub se estão lá:

- [ ] o código do projeto
- [ ] `monitoring/dashboard.png` (print do dashboard)
- [ ] `monitoring/grafana-dashboard.json` (dashboard exportado como código)

Depois é só colar o link do repositório na entrega da atividade.
