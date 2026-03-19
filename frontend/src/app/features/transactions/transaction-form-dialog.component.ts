import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogActions, MatDialogClose, MatDialogContent, MatDialogRef, MatDialogTitle } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { Transaction, TransactionRequest, TransactionType } from '../../core/models/transaction.model';
import { Account } from '../../core/models/account.model';
import { CategoryTreeNode } from '../../core/models/category.model';
import { CategorySelectComponent } from '../../shared/components/category-select.component';

export interface TransactionDialogData {
  transaction: Transaction | null;
  accounts: Account[];
  categories: CategoryTreeNode[];
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
    MatSelectModule,
    MatSlideToggleModule,
    CategorySelectComponent
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

        @if (showCategory()) {
          <app-category-select
            [categories]="data.categories"
            [value]="form.get('categoryId')?.value ?? null"
            (valueChange)="form.get('categoryId')?.setValue($event)"
          />
        }

        <mat-form-field>
          <mat-label>From Account</mat-label>
          <mat-select formControlName="fromAccountId">
            <mat-option [value]="null">None</mat-option>
            @for (account of data.accounts; track account.id) {
              <mat-option [value]="account.id">{{ account.name }}</mat-option>
            }
          </mat-select>
        </mat-form-field>

        <mat-form-field>
          <mat-label>To Account</mat-label>
          <mat-select formControlName="toAccountId">
            <mat-option [value]="null">None</mat-option>
            @for (account of data.accounts; track account.id) {
              <mat-option [value]="account.id">{{ account.name }}</mat-option>
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
export class TransactionFormDialogComponent {
  private readonly fb = inject(FormBuilder);
  private readonly dialogRef = inject(MatDialogRef<TransactionFormDialogComponent>);
  readonly data = inject<TransactionDialogData>(MAT_DIALOG_DATA);

  readonly types: TransactionType[] = ['INCOME', 'EXPENSE', 'TRANSFER'];
  readonly showCategory = signal(true);

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
    this.onTypeChange(this.form.get('type')?.value as TransactionType);
  }

  onTypeChange(type: TransactionType): void {
    const isTransfer = type === 'TRANSFER';
    this.showCategory.set(!isTransfer);

    if (isTransfer) {
      this.form.get('categoryId')?.setValue(null);
    }
  }

  save(): void {
    if (this.form.invalid) {
      return;
    }

    const value = this.form.getRawValue();
    const payload: TransactionRequest = {
      type: (value.type ?? 'EXPENSE') as TransactionType,
      amount: Number(value.amount ?? 0),
      date: value.date ?? new Date().toISOString().slice(0, 10),
      description: value.description || null,
      categoryId: value.type === 'TRANSFER' ? null : (value.categoryId ?? null),
      fromAccountId: value.fromAccountId,
      toAccountId: value.toAccountId
    };

    this.dialogRef.close(payload);
  }
}
