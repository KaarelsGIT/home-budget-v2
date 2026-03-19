import { Component, computed, inject, signal } from '@angular/core';
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
import { Account, TransferTarget } from '../../core/models/account.model';
import { CategoryTreeNode, CategoryType } from '../../core/models/category.model';
import { Transaction, TransactionRequest, TransactionType } from '../../core/models/transaction.model';

export interface TransactionDialogData {
  transaction: Transaction | null;
  myAccounts: Account[];
  transferTargets: TransferTarget[];
  incomeCategories: CategoryTreeNode[];
  expenseCategories: CategoryTreeNode[];
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
            <mat-label>Parent Category</mat-label>
            <mat-select formControlName="parentCategoryId" (valueChange)="onParentChange()">
              <mat-option [value]="null">None</mat-option>
              @for (category of rootCategories(); track category.id) {
                <mat-option [value]="category.id">{{ category.name }}</mat-option>
              }
            </mat-select>
          </mat-form-field>

          @if (availableSubCategories().length > 0) {
            <mat-form-field>
              <mat-label>Subcategory</mat-label>
              <mat-select formControlName="subCategoryId">
                <mat-option [value]="null">Parent only</mat-option>
                @for (category of availableSubCategories(); track category.id) {
                  <mat-option [value]="category.id">{{ category.name }}</mat-option>
                }
              </mat-select>
            </mat-form-field>
          }
        }

        @if (isExpense()) {
          <mat-form-field>
            <mat-label>From Account</mat-label>
            <mat-select formControlName="fromAccountId">
              @for (account of data.myAccounts; track account.id) {
                <mat-option [value]="account.id">{{ account.name }}</mat-option>
              }
            </mat-select>
          </mat-form-field>
        }

        @if (isIncome()) {
          <mat-form-field>
            <mat-label>To Account</mat-label>
            <mat-select formControlName="toAccountId">
              @for (account of data.myAccounts; track account.id) {
                <mat-option [value]="account.id">{{ account.name }}</mat-option>
              }
            </mat-select>
          </mat-form-field>
        }

        @if (isTransfer()) {
          <mat-form-field>
            <mat-label>From Account (my account)</mat-label>
            <mat-select formControlName="fromAccountId">
              @for (account of data.myAccounts; track account.id) {
                <mat-option [value]="account.id">{{ account.name }}</mat-option>
              }
            </mat-select>
          </mat-form-field>

          <mat-form-field>
            <mat-label>To Account (other user)</mat-label>
            <mat-select formControlName="toAccountId">
              @for (target of data.transferTargets; track target.id) {
                <mat-option [value]="target.id">{{ target.name }}</mat-option>
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
  readonly data = inject<TransactionDialogData>(MAT_DIALOG_DATA);

  readonly types: TransactionType[] = ['INCOME', 'EXPENSE', 'TRANSFER'];

  readonly isIncome = signal(false);
  readonly isExpense = signal(true);
  readonly isTransfer = signal(false);

  readonly form = this.fb.group({
    type: [this.data.transaction?.type ?? ('EXPENSE' as TransactionType), [Validators.required]],
    amount: [this.data.transaction?.amount ?? 0, [Validators.required, Validators.min(0.01)]],
    date: [this.data.transaction?.date ?? new Date().toISOString().slice(0, 10), [Validators.required]],
    description: [this.data.transaction?.description ?? ''],
    parentCategoryId: [this.data.transaction?.parentCategoryId ?? this.data.transaction?.categoryId ?? null],
    subCategoryId: [this.data.transaction?.subCategoryId ?? null],
    fromAccountId: [this.data.transaction?.fromAccountId ?? null],
    toAccountId: [this.data.transaction?.toAccountId ?? null]
  });

  readonly currentCategoryType = computed<CategoryType>(() =>
    this.isIncome() ? 'INCOME' : 'EXPENSE'
  );

  readonly rootCategories = computed(() =>
    this.currentCategoryType() === 'INCOME' ? this.data.incomeCategories : this.data.expenseCategories
  );

  readonly availableSubCategories = computed(() => {
    const parentId = this.form.get('parentCategoryId')?.value;
    const parent = this.rootCategories().find((category) => category.id === parentId);
    return parent?.children ?? [];
  });

  constructor() {
    this.onTypeChange((this.form.get('type')?.value ?? 'EXPENSE') as TransactionType);
  }

  onTypeChange(type: TransactionType): void {
    this.isIncome.set(type === 'INCOME');
    this.isExpense.set(type === 'EXPENSE');
    this.isTransfer.set(type === 'TRANSFER');

    if (type === 'TRANSFER') {
      this.form.get('parentCategoryId')?.setValue(null);
      this.form.get('subCategoryId')?.setValue(null);
      this.form.get('parentCategoryId')?.clearValidators();
      this.form.get('subCategoryId')?.clearValidators();

      this.form.get('fromAccountId')?.setValidators([Validators.required]);
      this.form.get('toAccountId')?.setValidators([Validators.required]);
    } else {
      this.form.get('parentCategoryId')?.setValidators([Validators.required]);
      this.form.get('subCategoryId')?.clearValidators();

      if (type === 'INCOME') {
        this.form.get('fromAccountId')?.setValue(null);
        this.form.get('toAccountId')?.setValidators([Validators.required]);
        this.form.get('fromAccountId')?.clearValidators();
      } else {
        this.form.get('toAccountId')?.setValue(null);
        this.form.get('fromAccountId')?.setValidators([Validators.required]);
        this.form.get('toAccountId')?.clearValidators();
      }
    }

    this.form.get('parentCategoryId')?.updateValueAndValidity();
    this.form.get('subCategoryId')?.updateValueAndValidity();
    this.form.get('fromAccountId')?.updateValueAndValidity();
    this.form.get('toAccountId')?.updateValueAndValidity();

    this.onParentChange();
  }

  onParentChange(): void {
    const currentSub = this.form.get('subCategoryId')?.value;
    const exists = this.availableSubCategories().some((sub) => sub.id === currentSub);
    if (!exists) {
      this.form.get('subCategoryId')?.setValue(null);
    }
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
      parentCategoryId: type === 'TRANSFER' ? null : (value.parentCategoryId ?? null),
      subCategoryId: type === 'TRANSFER' ? null : (value.subCategoryId ?? null),
      categoryId: type === 'TRANSFER' ? null : (value.subCategoryId ?? value.parentCategoryId ?? null),
      fromAccountId: value.fromAccountId,
      toAccountId: value.toAccountId
    };

    this.dialogRef.close(payload);
  }
}
