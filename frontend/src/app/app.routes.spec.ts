import { routes } from './app.routes';
import { adminGuard } from './core/guards/admin.guard';
import { authGuard } from './core/guards/auth.guard';

describe('app routes', () => {
  it('redirects legacy entry points to the collection-focused MVP shell', () => {
    const children = routes[0].children ?? [];

    const root = children.find((route) => route.path === '');
    const dashboard = children.find((route) => route.path === 'dashboard');
    const recommendations = children.find((route) => route.path === 'recommendations');
    const wildcard = children.find((route) => route.path === '**');

    expect(root?.redirectTo).toBe('home');
    expect(dashboard?.redirectTo).toBe('home');
    expect(recommendations?.redirectTo).toBe('wanted');
    expect(wildcard?.redirectTo).toBe('home');
  });

  it('protects private MVP and legacy routes while leaving public discovery open', () => {
    const children = routes[0].children ?? [];

    const home = children.find((route) => route.path === 'home');
    const catalog = children.find((route) => route.path === 'catalog');
    const editorialCatalog = children.find((route) => route.path === 'catalog/editorial');
    const editorialSeries = children.find(
      (route) => route.path === 'catalog/editorial/series/:seriesId'
    );
    const editorialItem = children.find(
      (route) => route.path === 'catalog/editorial/items/:itemId'
    );
    const editorialEdition = children.find(
      (route) => route.path === 'catalog/editorial/editions/:editionId'
    );
    const shops = children.find((route) => route.path === 'shops');
    const newShop = children.find((route) => route.path === 'shops/new');
    const newProduct = children.find((route) => route.path === 'catalog/new');
    const inventory = children.find((route) => route.path === 'shops/:shopId/inventory');
    const newInventoryProduct = children.find(
      (route) => route.path === 'shops/:shopId/inventory/new'
    );
    const editInventoryProduct = children.find(
      (route) => route.path === 'shops/:shopId/inventory/:shopProductId/edit'
    );
    const publicShopProduct = children.find((route) => route.path === 'shop-products/:shopProductId');
    const collections = children.find((route) => route.path === 'collections');
    const newCollection = children.find((route) => route.path === 'collections/new');
    const editCollection = children.find((route) => route.path === 'collections/:collectionId/edit');
    const newCollectionItem = children.find(
      (route) => route.path === 'collections/:collectionId/items/new'
    );
    const editCollectionItem = children.find(
      (route) => route.path === 'collections/:collectionId/items/:itemId/edit'
    );
    const collectionDetail = children.find((route) => route.path === 'collections/:collectionId');
    const wanted = children.find((route) => route.path === 'wanted');
    const profile = children.find((route) => route.path === 'profile');
    const reservations = children.find((route) => route.path === 'reservations');
    const reservationDetail = children.find((route) => route.path === 'reservations/:reservationId');
    const shopReservations = children.find((route) => route.path === 'shops/:shopId/reservations');
    const adminEditorial = children.find((route) => route.path === 'admin/editorial');
    const adminPublishers = children.find((route) => route.path === 'admin/editorial/publishers');
    const adminFranchises = children.find((route) => route.path === 'admin/editorial/franchises');
    const adminSeries = children.find((route) => route.path === 'admin/editorial/series');
    const adminItems = children.find((route) => route.path === 'admin/editorial/items');
    const adminEditions = children.find((route) => route.path === 'admin/editorial/editions');
    const adminCreators = children.find((route) => route.path === 'admin/editorial/creators');
    const adminCredits = children.find((route) => route.path === 'admin/editorial/credits');

    expect(home?.canActivate).toBeUndefined();
    expect(catalog?.canActivate).toBeUndefined();
    expect(editorialCatalog?.canActivate).toBeUndefined();
    expect(editorialSeries?.canActivate).toBeUndefined();
    expect(editorialItem?.canActivate).toBeUndefined();
    expect(editorialEdition?.canActivate).toBeUndefined();
    expect(shops?.canActivate).toContain(authGuard);
    expect(newShop?.canActivate).toContain(authGuard);
    expect(newProduct?.canActivate).toContain(authGuard);
    expect(inventory?.canActivate).toContain(authGuard);
    expect(newInventoryProduct?.canActivate).toContain(authGuard);
    expect(editInventoryProduct?.canActivate).toContain(authGuard);
    expect(publicShopProduct?.canActivate).toBeUndefined();
    expect(collections?.canActivate).toContain(authGuard);
    expect(newCollection?.canActivate).toContain(authGuard);
    expect(editCollection?.canActivate).toContain(authGuard);
    expect(newCollectionItem?.canActivate).toContain(authGuard);
    expect(editCollectionItem?.canActivate).toContain(authGuard);
    expect(collectionDetail?.canActivate).toBeUndefined();
    expect(wanted?.canActivate).toContain(authGuard);
    expect(profile?.canActivate).toContain(authGuard);
    expect(reservations?.canActivate).toContain(authGuard);
    expect(reservationDetail?.canActivate).toContain(authGuard);
    expect(shopReservations?.canActivate).toContain(authGuard);
    expect(adminEditorial?.canActivate).toEqual([authGuard, adminGuard]);
    expect(adminPublishers?.canActivate).toEqual([authGuard, adminGuard]);
    expect(adminFranchises?.canActivate).toEqual([authGuard, adminGuard]);
    expect(adminSeries?.canActivate).toEqual([authGuard, adminGuard]);
    expect(adminItems?.canActivate).toEqual([authGuard, adminGuard]);
    expect(adminEditions?.canActivate).toEqual([authGuard, adminGuard]);
    expect(adminCreators?.canActivate).toEqual([authGuard, adminGuard]);
    expect(adminCredits?.canActivate).toEqual([authGuard, adminGuard]);
  });
});
