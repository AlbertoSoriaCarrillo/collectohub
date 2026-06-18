import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { MainLayoutComponent } from './layout/main-layout.component';

export const routes: Routes = [
  {
    path: '',
    component: MainLayoutComponent,
    children: [
      {
        path: '',
        pathMatch: 'full',
        redirectTo: 'dashboard'
      },
      {
        path: 'login',
        loadComponent: () =>
          import('./features/auth/login/login.component').then((m) => m.LoginComponent)
      },
      {
        path: 'register',
        loadComponent: () =>
          import('./features/auth/register/register.component').then((m) => m.RegisterComponent)
      },
      {
        path: 'dashboard',
        canActivate: [authGuard],
        loadComponent: () =>
          import('./features/dashboard/dashboard.component').then((m) => m.DashboardComponent)
      },
      {
        path: 'shops',
        canActivate: [authGuard],
        loadComponent: () =>
          import('./features/shops/my-shops/my-shops.component').then((m) => m.MyShopsComponent)
      },
      {
        path: 'shops/new',
        canActivate: [authGuard],
        loadComponent: () =>
          import('./features/shops/shop-create/shop-create.component').then(
            (m) => m.ShopCreateComponent
          )
      },
      {
        path: 'shops/:id',
        loadComponent: () =>
          import('./features/shops/shop-detail/shop-detail.component').then(
            (m) => m.ShopDetailComponent
          )
      },
      {
        path: 'catalog',
        loadComponent: () =>
          import('./features/catalog/catalog-list/catalog-list.component').then(
            (m) => m.CatalogListComponent
          )
      },
      {
        path: 'catalog/new',
        canActivate: [authGuard],
        loadComponent: () =>
          import('./features/catalog/master-product-create/master-product-create.component').then(
            (m) => m.MasterProductCreateComponent
          )
      },
      {
        path: 'catalog/:id',
        loadComponent: () =>
          import('./features/catalog/master-product-detail/master-product-detail.component').then(
            (m) => m.MasterProductDetailComponent
          )
      },
      {
        path: '**',
        redirectTo: 'dashboard'
      }
    ]
  }
];
