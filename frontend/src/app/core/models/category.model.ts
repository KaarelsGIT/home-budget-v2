export type CategoryType = 'INCOME' | 'EXPENSE';

export interface Category {
  id: number;
  name: string;
  type: CategoryType;
  parentId?: number | null;
  userId: number;
}

export interface CategoryRequest {
  name: string;
  type: CategoryType;
  parentId?: number | null;
}

export interface CategoryTreeNode extends Category {
  children: CategoryTreeNode[];
}
