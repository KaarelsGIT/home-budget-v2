export type UserRole = 'ADMIN' | 'PARENT' | 'CHILD';
export type UserStatus = 'PENDING' | 'APPROVED';
export type LanguageCode = 'en' | 'et' | 'fi';

export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  password: string;
  role: Exclude<UserRole, 'ADMIN'>;
  parentId?: number | null;
}

export interface AuthResponse {
  token: string;
  userId: number;
  username: string;
  role: UserRole;
  status: UserStatus;
  familyId: string;
}

export interface AuthUser extends AuthResponse {
  language: LanguageCode;
}
