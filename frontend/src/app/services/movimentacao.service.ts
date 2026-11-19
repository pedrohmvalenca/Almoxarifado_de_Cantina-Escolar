import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Movimentacao, NovaMovimentacao } from '../models/movimentacao.model';

/**
 * Acesso às movimentações de um produto (/api/produtos/{id}/movimentacoes).
 * Uma saída maior que o saldo é recusada pela API com 409 e nada é gravado.
 */
@Injectable({ providedIn: 'root' })
export class MovimentacaoService {
  private http = inject(HttpClient);

  private url(produtoId: number): string {
    return `${environment.apiUrl}/produtos/${produtoId}/movimentacoes`;
  }

  historico(produtoId: number): Observable<Movimentacao[]> {
    return this.http.get<Movimentacao[]>(this.url(produtoId));
  }

  registrar(produtoId: number, m: NovaMovimentacao): Observable<Movimentacao> {
    return this.http.post<Movimentacao>(this.url(produtoId), m);
  }
}
