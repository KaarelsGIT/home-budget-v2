export type CategoryType = 'INCOME' | 'EXPENSE';

export interface Category {
  id: number;
  name: string;
  type: CategoryType;
  userId: number;
}

export interface CategoryRequest {
  name: string;
  type: CategoryType;
}
