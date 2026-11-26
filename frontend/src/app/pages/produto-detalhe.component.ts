import { DatePipe } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Movimentacao, TipoMov } from '../models/movimentacao.model';
import { Produto } from '../models/produto.model';
import { MovimentacaoService } from '../services/movimentacao.service';
import { ProdutoService, mensagemDeErro } from '../services/produto.service';

@Component({
  selector: 'app-produto-detalhe',
  standalone: true,
  imports: [FormsModule, DatePipe, RouterLink],
  template: `
    <p><a routerLink="/produtos">Voltar para a lista</a></p>

    @if (erroCarga) {
      <p class="msg erro">{{ erroCarga }}</p>
    } @else if (produto) {
      <h1>{{ produto.nome }}</h1>

      <div class="panel">
        <div class="muted">Saldo atual</div>
        <div class="saldo-grande">{{ produto.saldo }} {{ produto.unidade }}</div>
        <div class="muted">
          Mínimo: {{ produto.estoqueMinimo }} {{ produto.unidade }}
          @if (produto.saldo < produto.estoqueMinimo) { <span class="badge baixo">Abaixo do mínimo</span> }
        </div>
      </div>

      <form class="panel" #f="ngForm" (ngSubmit)="registrar()">
        <h2>Registrar movimentação</h2>
        @if (erro) { <p class="msg erro">{{ erro }}</p> }
        @if (sucesso) { <p class="msg ok">{{ sucesso }}</p> }

        <div class="row">
          <div class="field">
            <label for="tipo">Tipo</label>
            <select id="tipo" name="tipo" [(ngModel)]="tipo">
              <option value="entrada">Entrada (compra)</option>
              <option value="saida">Saída (uso na cozinha)</option>
            </select>
          </div>
          <div class="field">
            <label for="qtd">Quantidade ({{ produto.unidade }})</label>
            <input id="qtd" name="qtd" type="number" step="0.001" [(ngModel)]="quantidade" required />
            @if (quantidade !== null && quantidade <= 0) {
              <small class="muted">A quantidade deve ser maior que zero.</small>
            }
          </div>
        </div>
        <div class="field">
          <label for="motivo">Motivo</label>
          <input id="motivo" name="motivo" [(ngModel)]="motivo" required maxlength="200"
                 placeholder="Ex.: compra no mercado, almoço de terça" />
        </div>
        <button type="submit" [disabled]="f.invalid || !quantidade || quantidade <= 0 || !motivo.trim() || enviando">
          Registrar {{ tipo === 'entrada' ? 'entrada' : 'saída' }}
        </button>
      </form>

      <div class="panel">
        <h2>Histórico</h2>
        @if (erroHistorico) {
          <p class="msg erro">{{ erroHistorico }}</p>
        } @else if (historico.length === 0) {
          <p class="vazio">Nenhuma movimentação registrada ainda.</p>
        } @else {
          <table>
            <thead>
              <tr><th>Data</th><th>Tipo</th><th class="num">Quantidade</th><th>Motivo</th></tr>
            </thead>
            <tbody>
              @for (m of historico; track m.id) {
                <tr>
                  <td>{{ m.criadoEm | date: 'dd/MM/yyyy HH:mm' }}</td>
                  <td><span class="badge" [class.entrada]="m.tipo === 'entrada'" [class.saida]="m.tipo === 'saida'">{{ m.tipo === 'entrada' ? 'Entrada' : 'Saída' }}</span></td>
                  <td class="num">{{ m.quantidade }} {{ produto.unidade }}</td>
                  <td>{{ m.motivo }}</td>
                </tr>
              }
            </tbody>
          </table>
        }
      </div>
    }
  `,
})
export class ProdutoDetalheComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private produtos = inject(ProdutoService);
  private movs = inject(MovimentacaoService);

  id = 0;
  produto: Produto | null = null;
  historico: Movimentacao[] = [];

  tipo: TipoMov = 'entrada';
  quantidade: number | null = null;
  motivo = '';

  erro = '';
  sucesso = '';
  erroCarga = '';
  erroHistorico = '';
  enviando = false;

  ngOnInit(): void {
    this.id = Number(this.route.snapshot.paramMap.get('id'));
    this.carregar();
  }

  private carregar(): void {
    this.produtos.buscar(this.id).subscribe({
      next: (p) => (this.produto = p),
      error: (e) => (this.erroCarga = mensagemDeErro(e)),
    });
    this.movs.historico(this.id).subscribe({
      next: (h) => { this.historico = h; this.erroHistorico = ''; },
      error: (e) => { this.historico = []; this.erroHistorico = mensagemDeErro(e); },
    });
  }

  registrar(): void {
    const motivo = this.motivo.trim();
    if (!this.quantidade || this.quantidade <= 0 || !motivo) return;
    this.erro = '';
    this.sucesso = '';
    this.enviando = true;

    this.movs.registrar(this.id, { tipo: this.tipo, quantidade: this.quantidade, motivo }).subscribe({
      next: () => {
        this.sucesso = this.tipo === 'entrada' ? 'Entrada registrada.' : 'Saída registrada.';
        this.quantidade = null;
        this.motivo = '';
        this.enviando = false;
        this.carregar();
      },
      error: (e) => {
        // 409 = saldo insuficiente (nada foi registrado); 400 = dados inválidos
        this.erro = mensagemDeErro(e);
        this.enviando = false;
      },
    });
  }
}
