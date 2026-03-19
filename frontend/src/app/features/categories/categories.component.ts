import { Component, signal } from '@angular/core';
import { MatTreeModule } from '@angular/material/tree';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { CategoryService } from '../../core/services/category.service';
import { Category, CategoryRequest, CategoryTreeNode } from '../../core/models/category.model';
import { CategoryFormDialogComponent } from './category-form-dialog.component';
import { ConfirmDialogComponent } from '../../shared/components/confirm-dialog.component';

@Component({
  selector: 'app-categories',
  standalone: true,
  imports: [MatTreeModule, MatButtonModule, MatIconModule, MatCardModule, MatDialogModule, MatSnackBarModule],
  template: `
    <mat-card>
      <mat-card-header>
        <mat-card-title>Categories</mat-card-title>
        <button mat-flat-button color="primary" (click)="openCreateDialog()">Add Category</button>
      </mat-card-header>
      <mat-card-content>
        <mat-tree #tree [dataSource]="treeData()" [childrenAccessor]="childrenAccessor">
          <mat-tree-node *matTreeNodeDef="let node">
            <button mat-icon-button disabled></button>
            {{ node.name }}
            <button mat-icon-button (click)="openEditDialog(node)">
              <mat-icon>edit</mat-icon>
            </button>
            <button mat-icon-button color="warn" (click)="deleteCategory(node)">
              <mat-icon>delete</mat-icon>
            </button>
          </mat-tree-node>

          <mat-tree-node *matTreeNodeDef="let node; when: hasChild" matTreeNodeToggle>
            <button mat-icon-button matTreeNodeToggle>
              <mat-icon>{{ tree.isExpanded(node) ? 'expand_more' : 'chevron_right' }}</mat-icon>
            </button>
            {{ node.name }}
            <button mat-icon-button (click)="openEditDialog(node)">
              <mat-icon>edit</mat-icon>
            </button>
            <button mat-icon-button color="warn" (click)="deleteCategory(node)">
              <mat-icon>delete</mat-icon>
            </button>
          </mat-tree-node>
        </mat-tree>
      </mat-card-content>
    </mat-card>
  `
})
export class CategoriesComponent {
  readonly treeData = signal<CategoryTreeNode[]>([]);
  readonly allCategories = signal<Category[]>([]);

  readonly childrenAccessor = (node: CategoryTreeNode) => node.children;
  readonly hasChild = (_: number, node: CategoryTreeNode) => !!node.children?.length;

  constructor(
    private readonly categoryService: CategoryService,
    private readonly dialog: MatDialog,
    private readonly snackBar: MatSnackBar
  ) {
    this.loadCategories();
  }

  loadCategories(): void {
    this.categoryService.getCategories().subscribe((categories) => this.allCategories.set(categories));
    this.categoryService.getCategoryTree().subscribe((tree) => this.treeData.set(tree));
  }

  openCreateDialog(): void {
    const ref = this.dialog.open(CategoryFormDialogComponent, {
      data: { category: null, categories: this.allCategories() }
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
      data: { category, categories: this.allCategories() }
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
