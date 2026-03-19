export interface Category {
  id: number;
  name: string;
  parentId?: number | null;
  userId: number;
}

export interface CategoryRequest {
  name: string;
  parentId?: number | null;
}

export interface CategoryTreeNode extends Category {
  children: CategoryTreeNode[];
}
