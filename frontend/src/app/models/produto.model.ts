export interface Produto {
  id: number;
  nome: string;
  unidade: 'un' | 'kg' | 'L';
  estoqueMinimo: number;
  saldo: number;
}

export interface NovoProduto {
  nome: string;
  unidade: string;
  estoqueMinimo: number;
}
