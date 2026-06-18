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

    expect(shops?.canActivate).toContain(authGuard);
    expect(newShop?.canActivate).toContain(authGuard);
    expect(newProduct?.canActivate).toContain(authGuard);
    expect(inventory?.canActivate).toContain(authGuard);
    expect(newInventoryProduct?.canActivate).toContain(authGuard);
    expect(editInventoryProduct?.canActivate).toContain(authGuard);
    expect(publicShopProduct?.canActivate).toBeUndefined();
  });
});
