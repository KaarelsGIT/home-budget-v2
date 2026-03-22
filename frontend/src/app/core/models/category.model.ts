export type CategoryGroup = 'FAMILY' | 'CHILD';

export interface SubCategory {
  id: number;
  name: string;
  parentCategoryId: number;
  parentCategoryName: string;
}

export interface Category {
  id: number;
  name: string;
  group: CategoryGroup;
  ownerFamilyId: string;
  subCategories: SubCategory[];
}

export interface CategoryRequest {
  name: string;
  group: CategoryGroup;
}

export interface SubCategoryRequest {
  name: string;
  parentCategoryId: number;
}
