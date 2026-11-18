import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  template: `
    <header class="topo">
      <div class="topo-in">
        <strong class="marca">Almoxarifado da Cantina</strong>
        <nav>
          <a routerLink="/produtos" routerLinkActive="ativo" [routerLinkActiveOptions]="{ exact: true }">Produtos</a>
          <a routerLink="/produtos/abaixo-do-minimo" routerLinkActive="ativo">Estoque baixo</a>
          <a routerLink="/produtos/novo" routerLinkActive="ativo">Novo produto</a>
        </nav>
      </div>
    </header>
    <main class="page"><router-outlet /></main>
  `,
  styles: [`
    .topo { background: var(--brand); color: var(--brand-ink); }
    .topo-in { max-width: 860px; margin: 0 auto; padding: .8rem 1rem; display: flex; flex-wrap: wrap; gap: .5rem 1.5rem; align-items: center; justify-content: space-between; }
    .marca { font-size: 1.1rem; }
    nav { display: flex; gap: 1.1rem; }
    nav a { color: var(--brand-ink); text-decoration: none; padding: .2rem 0; border-bottom: 2px solid transparent; }
    nav a.ativo { border-bottom-color: #fff; font-weight: 600; }
  `],
})
export class AppComponent {}
