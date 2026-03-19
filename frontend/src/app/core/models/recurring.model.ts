export type RecurringFrequency = 'DAILY' | 'WEEKLY' | 'MONTHLY';

export interface RecurringTransaction {
  id: number;
  amount: number;
  categoryId?: number | null;
  frequency: RecurringFrequency;
  nextExecutionDate: string;
  active: boolean;
  accountId?: number | null;
  userId: number;
}

export interface RecurringTransactionRequest {
  amount: number;
  categoryId?: number | null;
  frequency: RecurringFrequency;
  nextExecutionDate: string;
  active: boolean;
  accountId?: number | null;
}
