export type TransactionType = 'INCOME' | 'EXPENSE' | 'TRANSFER';

export interface Transaction {
  id: number;
  amount: number;
  type: TransactionType;
  fromAccountId?: number | null;
  fromAccountName?: string | null;
  toAccountId?: number | null;
  toAccountName?: string | null;
  subCategoryId?: number | null;
  subCategoryName?: string | null;
  categoryName?: string | null;
  createdAt: string;
  createdById: number;
  createdByUsername: string;
}

export interface CreateTransactionRequest {
  type: TransactionType;
  amount: number;
  fromAccountId?: number | null;
  toAccountId?: number | null;
  subCategoryId?: number | null;
}

export interface TransactionFilter {
  startDate?: string;
  endDate?: string;
  subCategoryId?: number;
  type?: TransactionType;
  accountId?: number;
  userId?: number;
}
