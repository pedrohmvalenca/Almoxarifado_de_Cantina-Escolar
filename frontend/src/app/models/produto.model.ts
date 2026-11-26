/** Unidades de medida aceitas pela API. */
export type Unidade = 'un' | 'kg' | 'L';

/** Produto como retornado pela API (GET /api/produtos). */
export interface Produto {
  id: number;
  nome: string;
  unidade: Unidade;
  estoqueMinimo: number;
  saldo: number;
}

/** Corpo do POST /api/produtos. O saldo inicial é sempre 0 e definido pela API. */
export interface NovoProduto {
  nome: string;
  unidade: Unidade;
  estoqueMinimo: number;
}
