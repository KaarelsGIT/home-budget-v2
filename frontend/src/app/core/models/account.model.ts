export type AccountType = 'PERSONAL' | 'SHARED';

export interface Account {
  id: number;
  name: string;
  balance: number;
  type: AccountType;
  userId: number;
  parentAccountId?: number | null;
}

export interface AccountRequest {
  name: string;
  balance: number;
  type: AccountType;
  parentAccountId?: number | null;
}
