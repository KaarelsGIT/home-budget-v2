export type TransactionType = 'INCOME' | 'EXPENSE' | 'TRANSFER';

export interface Transaction {
  id: number;
  type: TransactionType;
  amount: number;
  date: string;
  description?: string | null;
  userId: number;
  categoryId?: number | null;
  categoryName?: string | null;
  parentCategoryId?: number | null;
  parentCategoryName?: string | null;
  subCategoryId?: number | null;
  subCategoryName?: string | null;
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
  parentCategoryId?: number | null;
  subCategoryId?: number | null;
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

export interface TransferRequest {
  fromAccountId: number;
  toAccountId: number;
  amount: number;
  date: string;
  description?: string | null;
}

export interface TransferResponse {
  transactionId: number;
  fromAccountId: number;
  fromAccountBalance: number;
  toAccountId: number;
  toAccountName: string;
}
