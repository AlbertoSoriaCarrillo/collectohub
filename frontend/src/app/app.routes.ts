import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { editorialAdminGuard } from './core/guards/editorial-admin.guard';
import { MainLayoutComponent } from './layout/main-layout.component';

export const routes: Routes = [
  {
    path: '',
    component: MainLayoutComponent,
    children: [
      {
        path: '',
        pathMatch: 'full',
        redirectTo: 'home'
      },
      {
        path: 'home',
        loadComponent: () =>
          import('./features/home/home.component').then((m) => m.HomeComponent)
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
        pathMatch: 'full',
        redirectTo: 'home'
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
        path: 'shops/:shopId/inventory',
        canActivate: [authGuard],
        loadComponent: () =>
          import('./features/inventory/shop-inventory/shop-inventory.component').then(
            (m) => m.ShopInventoryComponent
          )
      },
      {
        path: 'shops/:shopId/inventory/new',
        canActivate: [authGuard],
        loadComponent: () =>
          import('./features/inventory/shop-product-create/shop-product-create.component').then(
            (m) => m.ShopProductCreateComponent
          )
      },
      {
        path: 'shops/:shopId/inventory/:shopProductId/edit',
        canActivate: [authGuard],
        loadComponent: () =>
          import('./features/inventory/shop-product-edit/shop-product-edit.component').then(
            (m) => m.ShopProductEditComponent
          )
      },
      {
        path: 'shops/:shopId/reservations',
        canActivate: [authGuard],
        loadComponent: () =>
          import('./features/reservations/shop-reservations/shop-reservations.component').then(
            (m) => m.ShopReservationsComponent
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
        path: 'shop-products/:shopProductId',
        loadComponent: () =>
          import('./features/inventory/shop-product-detail/shop-product-detail.component').then(
            (m) => m.ShopProductDetailComponent
          )
      },
      {
        path: 'collections',
        canActivate: [authGuard],
        loadComponent: () =>
          import('./features/collections/my-collections/my-collections.component').then(
            (m) => m.MyCollectionsComponent
          )
      },
      {
        path: 'collections/new',
        canActivate: [authGuard],
        loadComponent: () =>
          import('./features/collections/collection-create/collection-create.component').then(
            (m) => m.CollectionCreateComponent
          )
      },
      {
        path: 'collections/:collectionId/edit',
        canActivate: [authGuard],
        loadComponent: () =>
          import('./features/collections/collection-edit/collection-edit.component').then(
            (m) => m.CollectionEditComponent
          )
      },
      {
        path: 'collections/:collectionId/items/new',
        canActivate: [authGuard],
        loadComponent: () =>
          import(
            './features/collections/collection-item-create/collection-item-create.component'
          ).then((m) => m.CollectionItemCreateComponent)
      },
      {
        path: 'collections/:collectionId/items/:itemId/edit',
        canActivate: [authGuard],
        loadComponent: () =>
          import('./features/collections/collection-item-edit/collection-item-edit.component').then(
            (m) => m.CollectionItemEditComponent
          )
      },
      {
        path: 'collections/:collectionId/series/:seriesId/progress',
        canActivate: [authGuard],
        loadComponent: () =>
          import(
            './features/collections/collection-series-progress/collection-series-progress.component'
          ).then((m) => m.CollectionSeriesProgressComponent)
      },
      {
        path: 'collections/:collectionId',
        loadComponent: () =>
          import('./features/collections/collection-detail/collection-detail.component').then(
            (m) => m.CollectionDetailComponent
          )
      },
      {
        path: 'wanted',
        canActivate: [authGuard],
        loadComponent: () =>
          import('./features/recommendations/recommendations/recommendations.component').then(
            (m) => m.RecommendationsComponent
          )
      },
      {
        path: 'recommendations',
        pathMatch: 'full',
        redirectTo: 'wanted'
      },
      {
        path: 'profile',
        canActivate: [authGuard],
        loadComponent: () =>
          import('./features/profile/profile.component').then((m) => m.ProfileComponent)
      },
      {
        path: 'admin/editorial',
        canActivate: [authGuard, editorialAdminGuard],
        loadComponent: () =>
          import(
            './features/admin/editorial/admin-editorial-shell/admin-editorial-shell.component'
          ).then((m) => m.AdminEditorialShellComponent)
      },
      {
        path: 'admin/editorial/publishers',
        canActivate: [authGuard, editorialAdminGuard],
        loadComponent: () =>
          import('./features/admin/editorial/admin-publishers/admin-publishers.component').then(
            (m) => m.AdminPublishersComponent
          )
      },
      {
        path: 'admin/editorial/franchises',
        canActivate: [authGuard, editorialAdminGuard],
        loadComponent: () =>
          import('./features/admin/editorial/admin-franchises/admin-franchises.component').then(
            (m) => m.AdminFranchisesComponent
          )
      },
      {
        path: 'admin/editorial/series',
        canActivate: [authGuard, editorialAdminGuard],
        loadComponent: () =>
          import('./features/admin/editorial/admin-series/admin-series.component').then(
            (m) => m.AdminSeriesComponent
          )
      },
      {
        path: 'admin/editorial/items',
        canActivate: [authGuard, editorialAdminGuard],
        loadComponent: () =>
          import('./features/admin/editorial/admin-items/admin-items.component').then(
            (m) => m.AdminItemsComponent
          )
      },
      {
        path: 'admin/editorial/editions',
        canActivate: [authGuard, editorialAdminGuard],
        loadComponent: () =>
          import('./features/admin/editorial/admin-editions/admin-editions.component').then(
            (m) => m.AdminEditionsComponent
          )
      },
      {
        path: 'admin/editorial/creators',
        canActivate: [authGuard, editorialAdminGuard],
        loadComponent: () =>
          import('./features/admin/editorial/admin-creators/admin-creators.component').then(
            (m) => m.AdminCreatorsComponent
          )
      },
      {
        path: 'admin/editorial/credits',
        canActivate: [authGuard, editorialAdminGuard],
        loadComponent: () =>
          import('./features/admin/editorial/admin-credits/admin-credits.component').then(
            (m) => m.AdminCreditsComponent
          )
      },
      {
        path: 'admin/editorial/relationships',
        canActivate: [authGuard, editorialAdminGuard],
        loadComponent: () =>
          import(
            './features/admin/editorial/admin-relationships/admin-relationships.component'
          ).then((m) => m.AdminRelationshipsComponent)
      },
      {
        path: 'admin/editorial/master-product-links',
        canActivate: [authGuard, editorialAdminGuard],
        loadComponent: () =>
          import(
            './features/admin/editorial/admin-master-product-links/admin-master-product-links.component'
          ).then((m) => m.AdminMasterProductLinksComponent)
      },
      {
        path: 'admin/editorial/data-quality',
        canActivate: [authGuard, editorialAdminGuard],
        loadComponent: () => import('./features/admin/editorial/admin-data-quality/admin-data-quality.component').then((m) => m.AdminDataQualityComponent)
      },
      {
        path: 'reservations',
        canActivate: [authGuard],
        loadComponent: () =>
          import('./features/reservations/my-reservations/my-reservations.component').then(
            (m) => m.MyReservationsComponent
          )
      },
      {
        path: 'reservations/:reservationId',
        canActivate: [authGuard],
        loadComponent: () =>
          import('./features/reservations/reservation-detail/reservation-detail.component').then(
            (m) => m.ReservationDetailComponent
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
        path: 'catalog/editorial',
        loadComponent: () =>
          import('./features/editorial/editorial-search/editorial-search.component').then(
            (m) => m.EditorialSearchComponent
          )
      },
      {
        path: 'catalog/editorial/series/:seriesId',
        loadComponent: () =>
          import(
            './features/editorial/editorial-series-detail/editorial-series-detail.component'
          ).then((m) => m.EditorialSeriesDetailComponent)
      },
      {
        path: 'catalog/editorial/items/:itemId',
        loadComponent: () =>
          import('./features/editorial/editorial-item-detail/editorial-item-detail.component').then(
            (m) => m.EditorialItemDetailComponent
          )
      },
      {
        path: 'catalog/editorial/editions/:editionId',
        loadComponent: () =>
          import(
            './features/editorial/editorial-edition-detail/editorial-edition-detail.component'
          ).then((m) => m.EditorialEditionDetailComponent)
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
        redirectTo: 'home'
      }
    ]
  }
];
