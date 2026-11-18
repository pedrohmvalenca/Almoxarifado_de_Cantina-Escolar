import { Component, OnInit, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Produto } from '../models/produto.model';
import { ProdutoService, mensagemDeErro } from '../services/produto.service';

@Component({
  selector: 'app-abaixo-minimo',
  standalone: true,
  imports: [RouterLink],
  template: `
    <h1>Produtos abaixo do estoque mínimo</h1>
    @if (erro) { <p class="msg erro">{{ erro }}</p> }
    <div class="panel">
      @if (carregando) {
        <p class="vazio">Carregando...</p>
      } @else if (produtos.length === 0) {
        <p class="vazio">Nenhum produto abaixo do mínimo. Tudo em ordem.</p>
      } @else {
        <table>
          <thead>
            <tr><th>Produto</th><th class="num">Saldo</th><th class="num">Mínimo</th></tr>
          </thead>
          <tbody>
            @for (p of produtos; track p.id) {
              <tr>
                <td><a [routerLink]="['/produtos', p.id]">{{ p.nome }}</a></td>
                <td class="num">{{ p.saldo }} {{ p.unidade }}</td>
                <td class="num">{{ p.estoqueMinimo }} {{ p.unidade }}</td>
              </tr>
            }
          </tbody>
        </table>
      }
    </div>
  `,
})
export class AbaixoMinimoComponent implements OnInit {
  private service = inject(ProdutoService);
  produtos: Produto[] = [];
  carregando = true;
  erro = '';

  ngOnInit(): void {
    this.service.abaixoDoMinimo().subscribe({
      next: (lista) => { this.produtos = lista; this.carregando = false; },
      error: (e) => { this.erro = mensagemDeErro(e); this.carregando = false; },
    });
  }
}
