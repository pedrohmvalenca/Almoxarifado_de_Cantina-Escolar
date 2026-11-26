# Novidades (adições sem alterar o que já existia)

Tudo abaixo é arquivo novo. Nenhum arquivo anterior foi editado.

## 1. Health check: `GET /health`
Exigido pelo requisito 5 do README. Responde `200 {"status":"UP","banco":"UP"}` quando a API e o banco
estão bem, e `503 {"status":"DOWN","banco":"DOWN"}` quando o banco não responde (sem detalhes internos).
Útil também como "Health check path" do App Service no Azure.

Arquivos: `web/HealthController.java`.

## 2. Resumo geral: `GET /api/resumo`
```json
{ "totalProdutos": 5, "abaixoDoMinimo": 2, "semEstoque": 3, "totalMovimentacoes": 2 }
```
Arquivos: `web/ResumoController.java`, `service/ResumoService.java`, `web/dto/ResumoResponse.java`.

## 3. Exportar estoque em CSV: `GET /api/relatorios/estoque.csv`
Baixa `estoque.csv` (separador `;`, decimal com vírgula, UTF-8 com BOM) que abre direto no Excel.
Colunas: `id;nome;unidade;estoque_minimo;saldo;situacao`.
Textos que começam com `=`, `+`, `-` ou `@` recebem um apóstrofo na frente para não virarem fórmula
no Excel (proteção contra CSV injection).

Arquivos: `web/RelatorioController.java`, `service/ExportacaoService.java`, teste `ExportacaoServiceTest.java`.

### Sobre os testes
Há um único teste novo, `ExportacaoServiceTest`, que é unitário puro: não sobe o Spring e não usa banco,
então não interfere nos testes existentes (`CriteriosAceitacaoTest`, `ValidacoesTest`). As rotas `/health`
e `/api/resumo` não têm teste automatizado de propósito, para não compartilhar o banco H2 com os testes atuais.

## 4. Consultas SQL úteis: `db/consultas-uteis.sql`
Produtos sem saldo, sem movimentação, saídas dos últimos 30 dias, saídas por motivo e dias de cobertura.

## 5. Tela "Painel do estoque" (frontend)
Arquivos novos: `models/resumo.model.ts`, `services/resumo.service.ts`, `pages/resumo-painel.component.ts`.

Para não mexer nos arquivos existentes, a tela ainda **não está ligada** ao menu. Quando quiserem ativar,
são duas linhas:

`frontend/src/app/app.routes.ts`
```ts
import { ResumoPainelComponent } from './pages/resumo-painel.component';
// dentro do array routes, antes de 'produtos/:id':
{ path: 'painel', component: ResumoPainelComponent },
```

`frontend/src/app/app.component.ts` (dentro do `<nav>`)
```html
<a routerLink="/painel" routerLinkActive="ativo">Painel</a>
```
