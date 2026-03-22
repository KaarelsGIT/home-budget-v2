import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { Category, CategoryGroup } from '../../core/models/category.model';
import { CategoryService } from '../../core/services/category.service';
import { I18nPipe } from '../../shared/pipes/i18n.pipe';

@Component({
  selector: 'app-categories',
  standalone: true,
  imports: [ReactiveFormsModule, MatButtonModule, MatCardModule, MatFormFieldModule, MatInputModule, MatSelectModule, I18nPipe],
  template: `
    <mat-card>
      <mat-card-header><mat-card-title>{{ 'categories.title' | i18n }}</mat-card-title></mat-card-header>
      <mat-card-content>
        <div style="display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:16px;margin-bottom:20px;">
          <form [formGroup]="categoryForm" (ngSubmit)="createCategory()" style="display:grid;gap:12px;">
            <mat-form-field><mat-label>{{ 'common.name' | i18n }}</mat-label><input matInput formControlName="name" /></mat-form-field>
            <mat-form-field>
              <mat-label>{{ 'categories.categoryGroup' | i18n }}</mat-label>
              <mat-select formControlName="group">
                <mat-option value="FAMILY">{{ 'categories.family' | i18n }}</mat-option>
                <mat-option value="CHILD">{{ 'categories.child' | i18n }}</mat-option>
              </mat-select>
            </mat-form-field>
            <button mat-flat-button type="submit">{{ 'categories.addCategory' | i18n }}</button>
          </form>

          <form [formGroup]="subCategoryForm" (ngSubmit)="createSubCategory()" style="display:grid;gap:12px;">
            <mat-form-field><mat-label>{{ 'common.name' | i18n }}</mat-label><input matInput formControlName="name" /></mat-form-field>
            <mat-form-field>
              <mat-label>{{ 'transactions.subCategory' | i18n }}</mat-label>
              <mat-select formControlName="parentCategoryId">
                @for (category of categories(); track category.id) { <mat-option [value]="category.id">{{ category.name }}</mat-option> }
              </mat-select>
            </mat-form-field>
            <button mat-flat-button type="submit">{{ 'categories.addSubCategory' | i18n }}</button>
          </form>
        </div>

        @for (category of categories(); track category.id) {
          <mat-card style="margin-bottom:12px;">
            <mat-card-title>{{ category.name }}</mat-card-title>
            <mat-card-subtitle>{{ category.group }}</mat-card-subtitle>
            <mat-card-content>
              <div style="display:flex;gap:8px;flex-wrap:wrap;">
                @for (sub of category.subCategories; track sub.id) {
                  <span style="padding:6px 10px;border-radius:999px;background:#fef3c7;">{{ sub.name }}</span>
                }
              </div>
            </mat-card-content>
          </mat-card>
        }
      </mat-card-content>
    </mat-card>
  `
})
export class CategoriesComponent {
  private readonly fb = inject(FormBuilder);
  private readonly categoryService = inject(CategoryService);

  readonly categories = signal<Category[]>([]);
  readonly categoryForm = this.fb.nonNullable.group({
    name: ['', Validators.required],
    group: ['FAMILY' as CategoryGroup, Validators.required]
  });
  readonly subCategoryForm = this.fb.nonNullable.group({
    name: ['', Validators.required],
    parentCategoryId: 0
  });

  constructor() {
    this.load();
  }

  createCategory(): void {
    if (this.categoryForm.invalid) return;
    this.categoryService.createCategory(this.categoryForm.getRawValue()).subscribe(() => {
      this.categoryForm.reset({ name: '', group: 'FAMILY' });
      this.load();
    });
  }

  createSubCategory(): void {
    if (this.subCategoryForm.invalid || !this.subCategoryForm.getRawValue().parentCategoryId) return;
    this.categoryService.createSubCategory(this.subCategoryForm.getRawValue()).subscribe(() => {
      this.subCategoryForm.reset({ name: '', parentCategoryId: 0 });
      this.load();
    });
  }

  private load(): void {
    this.categoryService.getCategories().subscribe((categories) => this.categories.set(categories));
  }
}
