import { HttpErrorResponse } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { LanguageService } from '../i18n/language.service';
import { ErrorResponse } from '../models/error-response.model';

@Injectable({
  providedIn: 'root'
})
export class ErrorMessageService {
  private readonly languageService = inject(LanguageService);

  toMessage(error: unknown): string {
    if (error instanceof HttpErrorResponse) {
      if (error.status === 0) {
        return this.languageService.translate('errors.network');
      }

      const body = error.error as Partial<ErrorResponse> | null;
      const detailsMessage = this.detailsToMessage(body?.details);
      return detailsMessage || body?.message || this.fallbackByStatus(error.status) || error.message;
    }

    return this.languageService.translate('errors.generic');
  }

  private fallbackByStatus(status: number): string | null {
    switch (status) {
      case 400:
        return this.languageService.translate('errors.badRequest');
      case 401:
        return this.languageService.translate('errors.unauthorized');
      case 403:
        return this.languageService.translate('errors.forbidden');
      case 404:
        return this.languageService.translate('errors.notFound');
      case 409:
        return this.languageService.translate('errors.conflict');
      case 500:
        return this.languageService.translate('errors.server');
      default:
        return null;
    }
  }

  private detailsToMessage(details: ErrorResponse['details']): string | null {
    if (!details) {
      return null;
    }

    if (Array.isArray(details)) {
      return details.filter(Boolean).join(' ');
    }

    return Object.values(details).filter(Boolean).join(' ');
  }
}
