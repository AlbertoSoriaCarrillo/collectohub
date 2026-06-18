import { routes } from './app.routes';
import { authGuard } from './core/guards/auth.guard';

describe('app routes', () => {
  it('protects private shop and catalog creation routes', () => {
    const children = routes[0].children ?? [];

    const shops = children.find((route) => route.path === 'shops');
    const newShop = children.find((route) => route.path === 'shops/new');
    const newProduct = children.find((route) => route.path === 'catalog/new');

    expect(shops?.canActivate).toContain(authGuard);
    expect(newShop?.canActivate).toContain(authGuard);
    expect(newProduct?.canActivate).toContain(authGuard);
  });
});
