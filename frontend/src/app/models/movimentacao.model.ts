export type TipoMov = 'entrada' | 'saida';

export interface Movimentacao {
  id: number;
  produtoId: number;
  tipo: TipoMov;
  quantidade: number;
  motivo: string;
  criadoEm: string;
}

export interface NovaMovimentacao {
  tipo: TipoMov;
  quantidade: number;
  motivo: string;
}
