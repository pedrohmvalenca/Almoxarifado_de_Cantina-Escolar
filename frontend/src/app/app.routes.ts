import { Routes } from '@angular/router';
import { AbaixoMinimoComponent } from './pages/abaixo-minimo.component';
import { ProdutoDetalheComponent } from './pages/produto-detalhe.component';
import { ProdutoFormComponent } from './pages/produto-form.component';
import { ProdutoListaComponent } from './pages/produto-lista.component';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'produtos' },
  { path: 'produtos', component: ProdutoListaComponent },
  { path: 'produtos/novo', component: ProdutoFormComponent },
  { path: 'produtos/abaixo-do-minimo', component: AbaixoMinimoComponent },
  { path: 'produtos/:id', component: ProdutoDetalheComponent },
  { path: '**', redirectTo: 'produtos' },
];
