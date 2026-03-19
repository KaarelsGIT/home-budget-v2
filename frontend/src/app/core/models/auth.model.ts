export type UserRole = 'PARENT' | 'CHILD';

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
  role: UserRole;
  parentId?: number | null;
}

export interface AuthResponse {
  token: string;
  userId: number;
  email: string;
  role: UserRole;
}

export interface AuthUser {
  token: string;
  userId: number;
  email: string;
  role: UserRole;
}
