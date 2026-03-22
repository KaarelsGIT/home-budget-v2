import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Category, CategoryGroup, CategoryRequest, SubCategory, SubCategoryRequest } from '../models/category.model';

@Injectable({ providedIn: 'root' })
export class CategoryService {
  private readonly baseUrl = `${environment.apiBaseUrl}/categories`;

  constructor(private readonly http: HttpClient) {}

  getCategories(group?: CategoryGroup): Observable<Category[]> {
    const params = group ? new HttpParams().set('group', group) : undefined;
    return this.http.get<Category[]>(this.baseUrl, { params });
  }

  createCategory(payload: CategoryRequest): Observable<Category> {
    return this.http.post<Category>(this.baseUrl, payload);
  }

  createSubCategory(payload: SubCategoryRequest): Observable<SubCategory> {
    return this.http.post<SubCategory>(`${this.baseUrl}/subcategories`, payload);
  }
}
