export type ReservationStatus =
  | 'PENDING'
  | 'ACCEPTED'
  | 'REJECTED'
  | 'CANCELLED'
  | 'EXPIRED'
  | 'COMPLETED';

export interface ReservationResponse {
  id: number;
  userId: number;
  userDisplayName: string;
  shopId: number;
  shopName: string;
  shopProductId: number;
  masterProductId: number | null;
  productName: string;
  catalogItemId?: number | null;
  catalogItemTitle?: string | null;
  catalogItemEditionId?: number | null;
  catalogItemEditionName?: string | null;
  quantity: number;
  status: ReservationStatus;
  userMessage: string | null;
  shopResponse: string | null;
  expiresAt: string | null;
  completedAt: string | null;
  createdAt: string;
}

export interface CreateReservationRequest {
  shopProductId: number;
  quantity?: number | null;
  userMessage?: string | null;
}

export interface UpdateReservationStatusRequest {
  status: ReservationStatus;
  shopResponse?: string | null;
}

export interface ReservationSearchFilters {
  status?: ReservationStatus | null;
  shopId?: number | null;
}

export interface ShopReservationSearchFilters {
  status?: ReservationStatus | null;
  userId?: number | null;
  shopProductId?: number | null;
}

export const RESERVATION_STATUSES: ReservationStatus[] = [
  'PENDING',
  'ACCEPTED',
  'REJECTED',
  'CANCELLED',
  'EXPIRED',
  'COMPLETED'
];

export const USER_CANCELLABLE_RESERVATION_STATUSES: ReservationStatus[] = ['PENDING', 'ACCEPTED'];
