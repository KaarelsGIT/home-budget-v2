export type CategoryType = 'INCOME' | 'EXPENSE';

export interface Category {
  id: number;
  name: string;
  type: CategoryType;
  userId: number;
  parentId?: number | null;
}

export interface CategoryRequest {
  name: string;
  type: CategoryType;
  parentId?: number | null;
}
