import { Transaction } from './transaction.model';

export interface OverviewTotals {
  income: number;
  expense: number;
  transferOut: number;
  net: number;
}

export interface OverviewMonthlyItem {
  month: number;
  income: number;
  expense: number;
  net: number;
}

export interface OverviewCategoryItem {
  parentCategory: string;
  subCategory: string;
  income: number;
  expense: number;
}

export interface FamilyOverviewResponse {
  year: number;
  month?: number | null;
  totals: OverviewTotals;
  monthly: OverviewMonthlyItem[];
  categories: OverviewCategoryItem[];
  transactions: Transaction[];
}
