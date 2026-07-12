import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../auth/auth.service';

export const editorialAdminGuard: CanActivateFn = (_route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (!authService.hasToken()) {
    return router.createUrlTree(['/login'], {
      queryParams: {
        returnUrl: state.url
      }
    });
  }

  if (!authService.hasEditorialAdminAccess()) {
    return router.createUrlTree(['/home']);
  }

  return true;
};
