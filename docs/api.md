# Documentação da API

URL base local: `http://localhost:8080/api`. Todas as requisições e respostas usam JSON.

> Quantidades e saldos são números decimais com até 3 casas (`NUMERIC(12,3)` no banco),
> então podem aparecer como `20`, `20.0` ou `20.000`, dependendo da rota.

## Formato padronizado de erro

Toda resposta de erro tem o mesmo formato, sem expor detalhes internos:

```json
{ "status": 409, "mensagem": "Saldo insuficiente para essa saída" }
```

| Status | Quando acontece |
| ----- | ----- |
| `400 Bad Request` | Campo inválido ou ausente, JSON malformado, id não numérico na URL |
| `404 Not Found` | Produto não encontrado |
| `409 Conflict` | Nome de produto já cadastrado, saldo insuficiente para a saída ou violação de regra do banco |
| `500 Internal Server Error` | Erro inesperado no servidor. A mensagem é sempre genérica e o detalhe fica só no log |

Nos erros de validação (`400`), `mensagem` lista os campos com problema no formato
`campo: motivo; campo: motivo`.

---

## Produtos

### `POST /api/produtos`: cadastrar produto

| Campo | Tipo | Regras |
| ----- | ----- | ----- |
| `nome` | texto | Obrigatório, até 100 caracteres, único (sem diferenciar maiúsculas/minúsculas) |
| `unidade` | texto | Obrigatório: `un`, `kg` ou `L` |
| `estoqueMinimo` | número | Opcional (padrão `0`), não pode ser negativo, até 3 casas decimais |

```http
POST /api/produtos
Content-Type: application/json

{ "nome": "Arroz", "unidade": "kg", "estoqueMinimo": 10 }
```

Resposta `201 Created` (o saldo começa sempre em zero):

```json
{ "id": 1, "nome": "Arroz", "unidade": "kg", "estoqueMinimo": 10, "saldo": 0 }
```

Erros: `400` (validação), `409` (nome já existe).

### `GET /api/produtos`: listar produtos

Resposta `200 OK`: lista de produtos ordenada por nome.

```json
[
  { "id": 1, "nome": "Arroz", "unidade": "kg", "estoqueMinimo": 10.000, "saldo": 20.000 },
  { "id": 2, "nome": "Feijão", "unidade": "kg", "estoqueMinimo": 8.000, "saldo": 3.000 }
]
```

### `GET /api/produtos/{id}`: consultar produto

Resposta `200 OK`: um produto no mesmo formato acima. Erros: `400` (id não numérico), `404`.

### `GET /api/produtos/abaixo-do-minimo`: produtos com estoque baixo

Resposta `200 OK`: produtos com `saldo < estoqueMinimo`, ordenados por nome.
Um produto com saldo **igual** ao mínimo não aparece na lista.

---

## Movimentações

### `POST /api/produtos/{id}/movimentacoes`: registrar entrada ou saída

| Campo | Tipo | Regras |
| ----- | ----- | ----- |
| `tipo` | texto | Obrigatório: `entrada` ou `saida` |
| `quantidade` | número | Obrigatório, maior que zero, até 3 casas decimais |
| `motivo` | texto | Obrigatório, até 200 caracteres |

```http
POST /api/produtos/1/movimentacoes
Content-Type: application/json

{ "tipo": "saida", "quantidade": 4, "motivo": "Almoço de terça" }
```

Resposta `201 Created`:

```json
{
  "id": 7,
  "produtoId": 1,
  "tipo": "saida",
  "quantidade": 4,
  "motivo": "Almoço de terça",
  "criadoEm": "2026-11-19T11:42:10.123"
}
```

Erros:

* `400`: quantidade menor ou igual a zero, tipo inválido ou motivo em branco.
* `404`: produto não existe.
* `409`: saída maior que o saldo atual. **Nada é gravado**: o saldo e o histórico continuam iguais.

O saldo é atualizado e a movimentação é gravada na mesma transação. A saída usa um
`UPDATE` condicional (`saldo >= quantidade`), então duas saídas simultâneas nunca deixam
o saldo negativo. Além disso, o banco tem um `CHECK (saldo >= 0)` como última proteção.

### `GET /api/produtos/{id}/movimentacoes`: histórico do produto

Resposta `200 OK`: lista de movimentações do produto, da mais recente para a mais antiga.
Erro: `404` se o produto não existir.
