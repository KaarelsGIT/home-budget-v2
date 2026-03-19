import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogActions, MatDialogClose, MatDialogContent, MatDialogRef, MatDialogTitle } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';
import { Category } from '../../core/models/category.model';

export interface CategoryDialogData {
  category: Category | null;
  categories: Category[];
}

@Component({
  selector: 'app-category-form-dialog',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatDialogTitle,
    MatDialogContent,
    MatDialogActions,
    MatDialogClose,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatSelectModule
  ],
  template: `
    <h2 mat-dialog-title>{{ data.category ? 'Edit Category' : 'New Category' }}</h2>
    <mat-dialog-content>
      <form [formGroup]="form" class="page-container">
        <mat-form-field>
          <mat-label>Name</mat-label>
          <input matInput formControlName="name" />
        </mat-form-field>

        <mat-form-field>
          <mat-label>Parent Category</mat-label>
          <mat-select formControlName="parentId">
            <mat-option [value]="null">None</mat-option>
            @for (category of availableParents; track category.id) {
              <mat-option [value]="category.id">{{ category.name }}</mat-option>
            }
          </mat-select>
        </mat-form-field>
      </form>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close="">Cancel</button>
      <button mat-flat-button color="primary" (click)="save()" [disabled]="form.invalid">Save</button>
    </mat-dialog-actions>
  `
})
export class CategoryFormDialogComponent {
  private readonly fb = inject(FormBuilder);
  private readonly dialogRef = inject(MatDialogRef<CategoryFormDialogComponent>);
  readonly data = inject<CategoryDialogData>(MAT_DIALOG_DATA);

  get availableParents(): Category[] {
    return this.data.categories.filter((category) => category.id !== this.data.category?.id);
  }

  readonly form = this.fb.group({
    name: [this.data.category?.name ?? '', [Validators.required]],
    parentId: [this.data.category?.parentId ?? null]
  });

  save(): void {
    if (this.form.invalid) {
      return;
    }

    this.dialogRef.close(this.form.getRawValue());
  }
}
