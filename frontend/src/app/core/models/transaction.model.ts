export type TransactionType = 'INCOME' | 'EXPENSE' | 'TRANSFER';

export interface Transaction {
  id: number;
  type: TransactionType;
  amount: number;
  date: string;
  description?: string | null;
  userId: number;
  categoryId?: number | null;
  fromAccountId?: number | null;
  toAccountId?: number | null;
  createdAt: string;
  updatedAt: string;
}

export interface TransactionRequest {
  type: TransactionType;
  amount: number;
  date: string;
  description?: string | null;
  categoryId?: number | null;
  fromAccountId?: number | null;
  toAccountId?: number | null;
}

export interface TransactionFilter {
  startDate?: string;
  endDate?: string;
  categoryId?: number;
  type?: TransactionType;
  accountId?: number;
  sortBy?: 'date' | 'amount';
  direction?: 'ASC' | 'DESC';
}
