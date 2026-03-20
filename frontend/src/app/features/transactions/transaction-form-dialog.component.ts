import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import {
  MAT_DIALOG_DATA,
  MatDialogActions,
  MatDialogClose,
  MatDialogContent,
  MatDialogRef,
  MatDialogTitle
} from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';
import { Account } from '../../core/models/account.model';
import { Category } from '../../core/models/category.model';
import { Transaction, TransactionRequest, TransactionType } from '../../core/models/transaction.model';
import { CategoryService } from '../../core/services/category.service';

export interface TransactionDialogData {
  transaction: Transaction | null;
  accounts: Account[];
}

@Component({
  selector: 'app-transaction-form-dialog',
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
    <h2 mat-dialog-title>{{ data.transaction ? 'Edit Transaction' : 'New Transaction' }}</h2>
    <mat-dialog-content>
      <form [formGroup]="form" class="page-container">
        <mat-form-field>
          <mat-label>Type</mat-label>
          <mat-select formControlName="type" (valueChange)="onTypeChange($event)">
            @for (type of types; track type) {
              <mat-option [value]="type">{{ type }}</mat-option>
            }
          </mat-select>
        </mat-form-field>

        <mat-form-field>
          <mat-label>Amount</mat-label>
          <input matInput type="number" formControlName="amount" />
        </mat-form-field>

        <mat-form-field>
          <mat-label>Date</mat-label>
          <input matInput type="date" formControlName="date" />
        </mat-form-field>

        <mat-form-field>
          <mat-label>Description</mat-label>
          <input matInput formControlName="description" />
        </mat-form-field>

        @if (!isTransfer()) {
          <mat-form-field>
            <mat-label>Category</mat-label>
            <mat-select formControlName="categoryId">
              @for (category of categories(); track category.id) {
                <mat-option [value]="category.id">{{ category.name }}</mat-option>
              }
            </mat-select>
          </mat-form-field>
        }

        @if (!isIncome()) {
          <mat-form-field>
            <mat-label>From Account</mat-label>
            <mat-select formControlName="fromAccountId">
              @for (account of data.accounts; track account.id) {
                <mat-option [value]="account.id">{{ account.name }}</mat-option>
              }
            </mat-select>
          </mat-form-field>
        }

        @if (!isExpense()) {
          <mat-form-field>
            <mat-label>To Account</mat-label>
            <mat-select formControlName="toAccountId">
              @for (account of data.accounts; track account.id) {
                <mat-option [value]="account.id">{{ account.name }}</mat-option>
              }
            </mat-select>
          </mat-form-field>
        }
      </form>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close="">Cancel</button>
      <button mat-flat-button color="primary" (click)="save()" [disabled]="form.invalid">Save</button>
    </mat-dialog-actions>
  `
})
export class TransactionFormDialogComponent {
  private readonly fb = inject(FormBuilder);
  private readonly dialogRef = inject(MatDialogRef<TransactionFormDialogComponent>);
  private readonly categoryService = inject(CategoryService);
  readonly data = inject<TransactionDialogData>(MAT_DIALOG_DATA);

  readonly types: TransactionType[] = ['INCOME', 'EXPENSE', 'TRANSFER'];
  readonly categories = signal<Category[]>([]);
  readonly isIncome = signal(false);
  readonly isExpense = signal(true);
  readonly isTransfer = signal(false);

  readonly form = this.fb.group({
    type: [this.data.transaction?.type ?? ('EXPENSE' as TransactionType), [Validators.required]],
    amount: [this.data.transaction?.amount ?? 0, [Validators.required, Validators.min(0.01)]],
    date: [this.data.transaction?.date ?? new Date().toISOString().slice(0, 10), [Validators.required]],
    description: [this.data.transaction?.description ?? ''],
    categoryId: [this.data.transaction?.categoryId ?? null],
    fromAccountId: [this.data.transaction?.fromAccountId ?? null],
    toAccountId: [this.data.transaction?.toAccountId ?? null]
  });

  constructor() {
    this.onTypeChange((this.form.get('type')?.value ?? 'EXPENSE') as TransactionType);
  }

  onTypeChange(type: TransactionType): void {
    this.isIncome.set(type === 'INCOME');
    this.isExpense.set(type === 'EXPENSE');
    this.isTransfer.set(type === 'TRANSFER');

    if (type === 'TRANSFER') {
      this.categories.set([]);
      this.form.get('categoryId')?.setValue(null);
      this.form.get('categoryId')?.clearValidators();

      this.form.get('fromAccountId')?.setValidators([Validators.required]);
      this.form.get('toAccountId')?.setValidators([Validators.required]);
    } else {
      const categoryType = type === 'INCOME' ? 'INCOME' : 'EXPENSE';
      this.categoryService.getCategoriesByType(categoryType).subscribe((items) => {
        this.categories.set(items);
      });

      this.form.get('categoryId')?.setValidators([Validators.required]);

      if (type === 'INCOME') {
        this.form.get('fromAccountId')?.setValue(null);
        this.form.get('fromAccountId')?.clearValidators();
        this.form.get('toAccountId')?.setValidators([Validators.required]);
      } else {
        this.form.get('toAccountId')?.setValue(null);
        this.form.get('toAccountId')?.clearValidators();
        this.form.get('fromAccountId')?.setValidators([Validators.required]);
      }
    }

    this.form.get('categoryId')?.updateValueAndValidity();
    this.form.get('fromAccountId')?.updateValueAndValidity();
    this.form.get('toAccountId')?.updateValueAndValidity();
  }

  save(): void {
    if (this.form.invalid) {
      return;
    }

    const value = this.form.getRawValue();
    const type = (value.type ?? 'EXPENSE') as TransactionType;

    const payload: TransactionRequest = {
      type,
      amount: Number(value.amount ?? 0),
      date: value.date ?? new Date().toISOString().slice(0, 10),
      description: value.description || null,
      categoryId: type === 'TRANSFER' ? null : (value.categoryId ?? null),
      fromAccountId: value.fromAccountId,
      toAccountId: value.toAccountId
    };

    this.dialogRef.close(payload);
  }
}
