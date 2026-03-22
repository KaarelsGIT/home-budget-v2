export type AccountMemberRole = 'OWNER' | 'MEMBER' | 'VIEWER';

export interface AccountMember {
  userId: number;
  username: string;
  role: AccountMemberRole;
}

export interface Account {
  id: number;
  name: string;
  ownerId: number;
  ownerUsername: string;
  isDefault: boolean;
  balance: number;
  members: AccountMember[];
}

export interface AccountRequest {
  name: string;
  isDefault: boolean;
  ownerId?: number | null;
}
