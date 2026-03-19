import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { map, Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Category, CategoryRequest, CategoryTreeNode, CategoryType } from '../models/category.model';

@Injectable({ providedIn: 'root' })
export class CategoryService {
  private readonly baseUrl = `${environment.apiBaseUrl}/categories`;

  constructor(private readonly http: HttpClient) {}

  getCategories(): Observable<Category[]> {
    return this.http.get<Category[]>(this.baseUrl);
  }

  getCategoriesByType(type: CategoryType): Observable<Category[]> {
    const params = new HttpParams().set('type', type);
    return this.http.get<Category[]>(`${environment.apiBaseUrl}/api/categories`, { params });
  }

  getCategoryById(id: number): Observable<Category> {
    return this.http.get<Category>(`${this.baseUrl}/${id}`);
  }

  createCategory(payload: CategoryRequest): Observable<Category> {
    return this.http.post<Category>(this.baseUrl, payload);
  }

  updateCategory(id: number, payload: CategoryRequest): Observable<Category> {
    return this.http.put<Category>(`${this.baseUrl}/${id}`, payload);
  }

  deleteCategory(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  getCategoryTree(): Observable<CategoryTreeNode[]> {
    return this.getCategories().pipe(map((categories) => this.buildTree(categories)));
  }

  getCategoryTreeByType(type: CategoryType): Observable<CategoryTreeNode[]> {
    return this.getCategoriesByType(type).pipe(map((categories) => this.buildTree(categories)));
  }

  private buildTree(categories: Category[]): CategoryTreeNode[] {
    const map = new Map<number, CategoryTreeNode>();

    categories.forEach((category) => {
      map.set(category.id, { ...category, children: [] });
    });

    const roots: CategoryTreeNode[] = [];

    map.forEach((node) => {
      if (node.parentId && map.has(node.parentId)) {
        map.get(node.parentId)?.children.push(node);
      } else {
        roots.push(node);
      }
    });

    return roots;
  }
}
