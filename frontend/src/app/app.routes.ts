import { Routes } from '@angular/router';
import { AbaixoMinimoComponent } from './pages/abaixo-minimo.component';
import { ProdutoDetalheComponent } from './pages/produto-detalhe.component';
import { ProdutoFormComponent } from './pages/produto-form.component';
import { ProdutoListaComponent } from './pages/produto-lista.component';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'produtos' },
  { path: 'produtos', component: ProdutoListaComponent, title: 'Produtos | Almoxarifado' },
  { path: 'produtos/novo', component: ProdutoFormComponent, title: 'Novo produto | Almoxarifado' },
  { path: 'produtos/abaixo-do-minimo', component: AbaixoMinimoComponent, title: 'Estoque baixo | Almoxarifado' },
  { path: 'produtos/:id', component: ProdutoDetalheComponent, title: 'Produto | Almoxarifado' },
  { path: '**', redirectTo: 'produtos' },
];
