export interface RecurringPayment {
  id: number;
  name: string;
  amount: number;
  subCategoryId: number;
  subCategoryName: string;
  categoryName: string;
  dueDay: number;
  ownerId: number;
  ownerUsername: string;
  active: boolean;
  paidThisMonth: boolean;
  paidTransactionId?: number | null;
}

export interface RecurringPaymentNotification {
  recurringPaymentId: number;
  name: string;
  amount: number;
  dueDay: number;
  subCategoryId: number;
  subCategoryName: string;
  categoryName: string;
  year: number;
  month: number;
  paid: boolean;
  paidTransactionId?: number | null;
}

export interface RecurringPaymentRequest {
  name: string;
  amount: number;
  subCategoryId: number;
  dueDay: number;
  active: boolean;
  ownerId?: number | null;
}
