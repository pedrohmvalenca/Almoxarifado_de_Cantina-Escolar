import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { NovoProduto, Produto } from '../models/produto.model';

/** Extrai a mensagem de erro padronizada da API ({status, mensagem}). */
export function mensagemDeErro(err: unknown): string {
  if (err instanceof HttpErrorResponse) {
    if (err.status === 0) return 'Não foi possível falar com a API.';
    return err.error?.mensagem ?? 'Erro inesperado.';
  }
  return 'Erro inesperado.';
}

/** Acesso às rotas de produtos da API (/api/produtos). */
@Injectable({ providedIn: 'root' })
export class ProdutoService {
  private http = inject(HttpClient);
  private base = `${environment.apiUrl}/produtos`;

  listar(): Observable<Produto[]> {
    return this.http.get<Produto[]>(this.base);
  }

  abaixoDoMinimo(): Observable<Produto[]> {
    return this.http.get<Produto[]>(`${this.base}/abaixo-do-minimo`);
  }

  buscar(id: number): Observable<Produto> {
    return this.http.get<Produto>(`${this.base}/${id}`);
  }

  criar(p: NovoProduto): Observable<Produto> {
    return this.http.post<Produto>(this.base, p);
  }
}
