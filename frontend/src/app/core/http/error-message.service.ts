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
      return detailsMessage || body?.message || this.fallbackByStatus(error.status) || error.message;
    }

    return 'La operacion no se pudo completar.';
  }

  private fallbackByStatus(status: number): string | null {
    switch (status) {
      case 400:
        return 'Revisa los datos del formulario.';
      case 401:
        return 'Inicia sesion para continuar.';
      case 403:
        return 'No tienes permisos para realizar esta accion.';
      case 404:
        return 'No se encontro el recurso solicitado.';
      case 409:
        return 'Ya existe un recurso con esos datos.';
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
