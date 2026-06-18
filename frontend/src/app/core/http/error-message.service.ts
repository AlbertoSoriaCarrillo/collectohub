import { HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { ErrorResponse } from '../models/error-response.model';

@Injectable({
  providedIn: 'root'
})
export class ErrorMessageService {
  toMessage(error: unknown): string {
    if (error instanceof HttpErrorResponse) {
      if (error.status === 0) {
        return 'No se pudo conectar con el backend local.';
      }

      const body = error.error as Partial<ErrorResponse> | null;
      const detailsMessage = this.detailsToMessage(body?.details);
      return detailsMessage || body?.message || error.message || 'La operacion no se pudo completar.';
    }

    return 'La operacion no se pudo completar.';
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
