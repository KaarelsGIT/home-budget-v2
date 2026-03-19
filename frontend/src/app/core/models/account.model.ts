export interface Account {
  id: number;
  name: string;
  balance: number;
  currency: string;
  userId: number;
}

export interface AccountRequest {
  name: string;
  balance: number;
  currency: string;
}

export interface TransferTarget {
  id: number;
  name: string;
}
