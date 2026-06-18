import { routes } from './app.routes';
import { authGuard } from './core/guards/auth.guard';

describe('app routes', () => {
  it('protects private shop, catalog and inventory routes', () => {
    const children = routes[0].children ?? [];

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
    const recommendations = children.find((route) => route.path === 'recommendations');
    const reservations = children.find((route) => route.path === 'reservations');
    const reservationDetail = children.find((route) => route.path === 'reservations/:reservationId');
    const shopReservations = children.find((route) => route.path === 'shops/:shopId/reservations');

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
    expect(recommendations?.canActivate).toContain(authGuard);
    expect(reservations?.canActivate).toContain(authGuard);
    expect(reservationDetail?.canActivate).toContain(authGuard);
    expect(shopReservations?.canActivate).toContain(authGuard);
  });
});
