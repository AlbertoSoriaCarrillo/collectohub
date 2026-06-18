import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { TokenStorageService } from './token-storage.service';

export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const tokenStorage = inject(TokenStorageService);
  const router = inject(Router);
  const token = tokenStorage.getAccessToken();

  const authenticatedRequest = token
    ? request.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`
        }
      })
    : request;

  return next(authenticatedRequest).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401 && !isAuthEndpoint(request.url)) {
        tokenStorage.clear();
        void router.navigateByUrl('/login');
      }

      return throwError(() => error);
    })
  );
};

function isAuthEndpoint(url: string): boolean {
  return url.includes('/api/auth/login') || url.includes('/api/auth/register');
}
