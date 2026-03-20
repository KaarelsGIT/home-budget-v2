import { Component, EventEmitter, Input, Output } from '@angular/core';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { Category } from '../../core/models/category.model';

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
        @for (category of categories; track category.id) {
          <mat-option [value]="category.id">{{ category.name }}</mat-option>
        }
      </mat-select>
    </mat-form-field>
  `
})
export class CategorySelectComponent {
  @Input() categories: Category[] = [];
  @Input() label = 'Category';
  @Input() value: number | null = null;
  @Input() allowEmpty = true;
  @Output() valueChange = new EventEmitter<number | null>();
}
