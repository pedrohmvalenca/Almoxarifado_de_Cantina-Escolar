export type TipoMov = 'entrada' | 'saida';

/** Movimentação registrada, como retornada no histórico do produto. */
export interface Movimentacao {
  id: number;
  produtoId: number;
  tipo: TipoMov;
  quantidade: number;
  motivo: string;
  criadoEm: string;
}

/** Corpo do POST /api/produtos/{id}/movimentacoes. A quantidade deve ser maior que zero. */
export interface NovaMovimentacao {
  tipo: TipoMov;
  quantidade: number;
  motivo: string;
}
