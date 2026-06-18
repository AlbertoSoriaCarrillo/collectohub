export interface ShopMemberResponse {
  id: number;
  userId: number;
  role: string;
  status: string;
}

export interface ShopResponse {
  id: number;
  name: string;
  description: string | null;
  contactEmail: string | null;
  contactPhone: string | null;
  country: string | null;
  currency: string;
  defaultReservationExpirationHours: number;
  logoUrl: string | null;
  status: string;
  currentUserMembership: ShopMemberResponse | null;
}

export interface CreateShopRequest {
  name: string;
  description?: string | null;
  contactEmail?: string | null;
  contactPhone?: string | null;
  country?: string | null;
  currency?: string | null;
  defaultReservationExpirationHours?: number | null;
  logoUrl?: string | null;
}

export type UpdateShopRequest = Partial<CreateShopRequest>;
