import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ProdutoService, mensagemDeErro } from '../services/produto.service';

@Component({
  selector: 'app-produto-form',
  standalone: true,
  imports: [FormsModule],
  template: `
    <h1>Novo produto</h1>
    <form class="panel" #f="ngForm" (ngSubmit)="salvar()">
      @if (erro) { <p class="msg erro">{{ erro }}</p> }

      <div class="field">
        <label for="nome">Nome</label>
        <input id="nome" name="nome" [(ngModel)]="nome" required maxlength="100" />
      </div>

      <div class="row">
        <div class="field">
          <label for="unidade">Unidade</label>
          <select id="unidade" name="unidade" [(ngModel)]="unidade" required>
            <option value="un">Unidade (un)</option>
            <option value="kg">Quilo (kg)</option>
            <option value="L">Litro (L)</option>
          </select>
        </div>
        <div class="field">
          <label for="minimo">Estoque mínimo</label>
          <input id="minimo" name="minimo" type="number" min="0" step="0.001" [(ngModel)]="estoqueMinimo" required />
        </div>
      </div>

      <p class="muted">O saldo inicial é 0. Registre uma entrada depois de cadastrar.</p>
      <button type="submit" [disabled]="f.invalid || enviando">Cadastrar produto</button>
    </form>
  `,
})
export class ProdutoFormComponent {
  private service = inject(ProdutoService);
  private router = inject(Router);

  nome = '';
  unidade = 'un';
  estoqueMinimo = 0;
  erro = '';
  enviando = false;

  salvar(): void {
    this.erro = '';
    this.enviando = true;
    this.service.criar({ nome: this.nome, unidade: this.unidade, estoqueMinimo: this.estoqueMinimo }).subscribe({
      next: (p) => this.router.navigate(['/produtos', p.id]),
      error: (e) => {
        // 409 = nome já existe (CA5); 400 = validação
        this.erro = mensagemDeErro(e);
        this.enviando = false;
      },
    });
  }
}
