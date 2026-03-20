import { Component, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { CategoryService } from '../../core/services/category.service';
import { Category, CategoryRequest } from '../../core/models/category.model';
import { CategoryFormDialogComponent } from './category-form-dialog.component';
import { ConfirmDialogComponent } from '../../shared/components/confirm-dialog.component';

@Component({
  selector: 'app-categories',
  standalone: true,
  imports: [MatTableModule, MatButtonModule, MatIconModule, MatCardModule, MatDialogModule, MatSnackBarModule],
  template: `
    <mat-card>
      <mat-card-header>
        <mat-card-title>Categories</mat-card-title>
        <button mat-flat-button color="primary" (click)="openCreateDialog()">Add Category</button>
      </mat-card-header>
      <mat-card-content>
        <table mat-table [dataSource]="categories()" class="full-width">
          <ng-container matColumnDef="name">
            <th mat-header-cell *matHeaderCellDef>Name</th>
            <td mat-cell *matCellDef="let row">{{ row.name }}</td>
          </ng-container>

          <ng-container matColumnDef="type">
            <th mat-header-cell *matHeaderCellDef>Type</th>
            <td mat-cell *matCellDef="let row">{{ row.type }}</td>
          </ng-container>

          <ng-container matColumnDef="actions">
            <th mat-header-cell *matHeaderCellDef></th>
            <td mat-cell *matCellDef="let row">
              <button mat-icon-button (click)="openEditDialog(row)">
                <mat-icon>edit</mat-icon>
              </button>
              <button mat-icon-button color="warn" (click)="deleteCategory(row)">
                <mat-icon>delete</mat-icon>
              </button>
            </td>
          </ng-container>

          <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
          <tr mat-row *matRowDef="let row; columns: displayedColumns"></tr>
        </table>
      </mat-card-content>
    </mat-card>
  `
})
export class CategoriesComponent {
  readonly displayedColumns = ['name', 'type', 'actions'];
  readonly categories = signal<Category[]>([]);

  constructor(
    private readonly categoryService: CategoryService,
    private readonly dialog: MatDialog,
    private readonly snackBar: MatSnackBar
  ) {
    this.loadCategories();
  }

  loadCategories(): void {
    this.categoryService.getCategories().subscribe((categories) => this.categories.set(categories));
  }

  openCreateDialog(): void {
    const ref = this.dialog.open(CategoryFormDialogComponent, {
      data: { category: null }
    });

    ref.afterClosed().subscribe((payload: CategoryRequest | undefined) => {
      if (!payload) {
        return;
      }

      this.categoryService.createCategory(payload).subscribe(() => {
        this.snackBar.open('Category created', 'Close', { duration: 2500 });
        this.loadCategories();
      });
    });
  }

  openEditDialog(category: Category): void {
    const ref = this.dialog.open(CategoryFormDialogComponent, {
      data: { category }
    });

    ref.afterClosed().subscribe((payload: CategoryRequest | undefined) => {
      if (!payload) {
        return;
      }

      this.categoryService.updateCategory(category.id, payload).subscribe(() => {
        this.snackBar.open('Category updated', 'Close', { duration: 2500 });
        this.loadCategories();
      });
    });
  }

  deleteCategory(category: Category): void {
    const ref = this.dialog.open(ConfirmDialogComponent, {
      data: { title: 'Delete Category', message: `Delete category ${category.name}?` }
    });

    ref.afterClosed().subscribe((confirmed: boolean) => {
      if (!confirmed) {
        return;
      }

      this.categoryService.deleteCategory(category.id).subscribe(() => {
        this.snackBar.open('Category deleted', 'Close', { duration: 2500 });
        this.loadCategories();
      });
    });
  }
}
