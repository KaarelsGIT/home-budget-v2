import { Component, EventEmitter, Input, Output } from '@angular/core';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { CategoryTreeNode } from '../../core/models/category.model';

@Component({
  selector: 'app-category-select',
  standalone: true,
  imports: [MatFormFieldModule, MatSelectModule],
  template: `
    <mat-form-field>
      <mat-label>{{ label }}</mat-label>
      <mat-select [value]="value" (valueChange)="valueChange.emit($event)">
        @if (allowEmpty) {
          <mat-option [value]="null">None</mat-option>
        }
        @for (root of categories; track root.id) {
          <mat-option [value]="root.id">{{ root.name }}</mat-option>
          @for (child of root.children; track child.id) {
            <mat-option [value]="child.id">&nbsp;&nbsp;└ {{ child.name }}</mat-option>
          }
        }
      </mat-select>
    </mat-form-field>
  `
})
export class CategorySelectComponent {
  @Input() categories: CategoryTreeNode[] = [];
  @Input() label = 'Category';
  @Input() value: number | null = null;
  @Input() allowEmpty = true;
  @Output() valueChange = new EventEmitter<number | null>();
}
