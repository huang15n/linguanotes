export type AccountRole = "USER" | "TEACHER" | "ADMIN";
export type AccountStatus = "ACTIVE" | "PENDING" | "SUSPENDED" | "CLOSED";

export interface Account {
  id: number;
  name: string;
  email: string;
  role: AccountRole;
  firstName: string | null;
  lastName: string | null;
  displayName: string | null;
  avatarUrl: string | null;
  country: string | null;
  timezone: string | null;
  nativeLanguageId: number | null;
  nativeLanguageName: string | null;
  status: AccountStatus;
  lastLoginAt: string | null;
}

export interface RegisterRequest {
  name: string;
  firstName?: string | null;
  lastName?: string | null;
  email: string;
  password: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface ProfileUpdate {
  name: string;
  firstName: string | null;
  lastName: string | null;
  displayName: string | null;
  avatarUrl: string | null;
  country: string | null;
  timezone: string | null;
  nativeLanguageId: number | null;
}
