export interface UserStatsItem {
  userId: number;
  username: string;
  income: number;
  expense: number;
  net: number;
}

export interface StatsSummary {
  income: number;
  expense: number;
  net: number;
  perUser: UserStatsItem[];
}

export interface StatsMonthlyItem {
  month: number;
  income: number;
  expense: number;
  net: number;
}

export interface StatsCategoryItem {
  categoryName: string;
  subCategoryName: string;
  amount: number;
}

export interface StatsTrendItem {
  label: string;
  income: number;
  expense: number;
  net: number;
}
