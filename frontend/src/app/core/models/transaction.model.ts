export type TransactionType = 'INCOME' | 'EXPENSE' | 'TRANSFER';

export interface Transaction {
  id: number;
  type: TransactionType;
  amount: number;
  userId: number;
  categoryId?: number | null;
  categoryName?: string | null;
  fromAccountId?: number | null;
  toAccountId?: number | null;
  createdAt: string;
}

export interface TransactionRequest {
  type: TransactionType;
  amount: number;
  categoryId?: number | null;
  fromAccountId?: number | null;
  toAccountId?: number | null;
}

export interface TransferRequestApi {
  fromAccountId: number;
  toAccountId: number;
  amount: number;
}

export interface TransactionFilter {
  startDate?: string;
  endDate?: string;
  categoryId?: number;
  type?: TransactionType;
  accountId?: number;
  sortBy?: 'createdAt' | 'amount';
  direction?: 'ASC' | 'DESC';
}
