# Divisão da equipe e commits sugeridos

Cada pessoa mexe só na sua pasta. Padrão de commit: `feat|fix|chore|test|docs(escopo): descrição`.
`main` protegida, tudo via Pull Request com 1 aprovação.

## Pessoa 1: Banco de dados + Azure (`feature/db-azure`)
Arquivos: `docker-compose.yml`, `backend/src/main/resources/db/migration/*`, `db/*`, `docs/azure.md`
1. `chore(db): docker-compose com postgres local`
2. `feat(db): migration V1 cria tabela produtos`
3. `feat(db): migration V2 cria tabela movimentacoes`
4. `feat(db): script de seed com produtos de exemplo`
5. `docs(azure): provisiona PostgreSQL e App Service`
6. `docs(db): queries de evidência em db/evidencias.sql`

## Pessoa 2: Backend base + Produtos (`feature/back-produtos`)
Arquivos: `backend/pom.xml`, `application.yml`, `AlmoxarifadoApplication`, `config/`, `model/Produto`, `repository/ProdutoRepository` (consultas de leitura), `service/ProdutoService`, `web/ProdutoController`, `web/dto/Produto*`, `exception/*`
1. `chore(back): inicia projeto Spring Boot`
2. `chore(back): application.yml com variáveis de ambiente e CORS`
3. `feat(back): entidade Produto e ProdutoRepository`
4. `feat(back): POST /api/produtos com validação (RF1)`
5. `feat(back): GlobalExceptionHandler (400, 404, 409)`
6. `feat(back): nome duplicado retorna 409 (CA5)`
7. `feat(back): GET /api/produtos e /{id} (RF4)`
8. `feat(back): GET /api/produtos/abaixo-do-minimo (RF5)`

## Pessoa 3: Backend Movimentações, regra central (`feature/back-movimentacoes`)
Arquivos: `model/Movimentacao`, `repository/MovimentacaoRepository`, `baixarSaldo/somarSaldo` no `ProdutoRepository`, `service/MovimentacaoService`, `web/MovimentacaoController`, `web/dto/Movimentacao*`, `src/test`
1. `feat(back): entidade Movimentacao e repository`
2. `feat(back): baixarSaldo e somarSaldo (UPDATE atômico)`
3. `feat(back): POST /movimentacoes de entrada (RF2)`
4. `feat(back): saída recusada com 409 se saldo insuficiente (RF3)`
5. `feat(back): quantidade <= 0 retorna 400 (CA3)`
6. `feat(back): GET /movimentacoes (RF4)`
7. `test(back): testes de CA1 a CA5`

## Pessoa 4: Frontend base + Produtos (`feature/front-produtos`)
Arquivos: `frontend/` (config, `app.*`, `models/produto`, `services/produto.service`, `pages/produto-lista`, `produto-form`, `abaixo-minimo`, `styles.css`)
1. `chore(front): cria projeto Angular com rotas e proxy`
2. `feat(front): environment com apiUrl`
3. `feat(front): ProdutoService e modelos`
4. `feat(front): layout base (navbar + router-outlet)`
5. `feat(front): listagem de produtos com saldo`
6. `feat(front): formulário de cadastro (RF1)`
7. `feat(front): exibe erro 409 de nome duplicado (CA5)`
8. `feat(front): tela abaixo do mínimo (RF5, CA4)`

## Pessoa 5: Frontend Movimentações + CI + Evidências (`feature/front-movimentacoes`)
Arquivos: `models/movimentacao`, `services/movimentacao.service`, `pages/produto-detalhe`, `.github/workflows/*`, `README.md`
1. `feat(front): MovimentacaoService`
2. `feat(front): detalhe do produto com saldo atual (RF4)`
3. `feat(front): histórico de movimentações`
4. `feat(front): formulário de entrada/saída com qtd > 0`
5. `feat(front): mensagens para saída recusada (409) e qtd inválida (400)`
6. `ci: GitHub Actions do backend`
7. `ci: GitHub Actions do frontend`
8. `docs: README final e prints das evidências E1 a E6`

## Ordem recomendada
P1 e P2 primeiro (base). P3 depende do `GlobalExceptionHandler` da P2. P4 e P5 trabalham em paralelo e testam contra a API local.
