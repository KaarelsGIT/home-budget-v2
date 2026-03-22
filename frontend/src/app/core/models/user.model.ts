import { UserRole, UserStatus } from './auth.model';

export interface UserSummary {
  id: number;
  username: string;
  role: UserRole;
  status: UserStatus;
}

export interface UserDetails extends UserSummary {
  createdAt: string;
  parentId?: number | null;
  familyId: string;
}
