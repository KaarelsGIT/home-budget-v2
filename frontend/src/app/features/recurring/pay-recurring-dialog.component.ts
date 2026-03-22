import { Component, Inject, inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { Account } from '../../core/models/account.model';
import { RecurringPaymentNotification } from '../../core/models/recurring.model';
import { SubCategory } from '../../core/models/category.model';
import { CreateTransactionRequest } from '../../core/models/transaction.model';
import { I18nPipe } from '../../shared/pipes/i18n.pipe';

export interface PayRecurringDialogData {
  notification: RecurringPaymentNotification;
  accounts: Account[];
  subCategories: SubCategory[];
}

@Component({
  selector: 'app-pay-recurring-dialog',
  standalone: true,
  imports: [ReactiveFormsModule, MatDialogModule, MatButtonModule, MatFormFieldModule, MatInputModule, MatSelectModule, I18nPipe],
  template: `
    <h2 mat-dialog-title>{{ 'recurring.paymentModal' | i18n }}</h2>
    <mat-dialog-content>
      <form [formGroup]="form" style="display:grid;gap:12px;padding-top:8px;">
        <mat-form-field><mat-label>{{ 'common.amount' | i18n }}</mat-label><input matInput type="number" formControlName="amount" /></mat-form-field>
        <mat-form-field>
          <mat-label>{{ 'transactions.sourceAccount' | i18n }}</mat-label>
          <mat-select formControlName="fromAccountId">
            @for (account of data.accounts; track account.id) { <mat-option [value]="account.id">{{ account.name }}</mat-option> }
          </mat-select>
        </mat-form-field>
        <mat-form-field>
          <mat-label>{{ 'transactions.subCategory' | i18n }}</mat-label>
          <mat-select formControlName="subCategoryId">
            @for (sub of data.subCategories; track sub.id) { <mat-option [value]="sub.id">{{ sub.parentCategoryName }} / {{ sub.name }}</mat-option> }
          </mat-select>
        </mat-form-field>
      </form>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button (click)="close()">{{ 'common.cancel' | i18n }}</button>
      <button mat-flat-button (click)="confirm()">{{ 'common.confirm' | i18n }}</button>
    </mat-dialog-actions>
  `
})
export class PayRecurringDialogComponent {
  private readonly fb = inject(FormBuilder);
  private readonly dialogRef = inject(MatDialogRef<PayRecurringDialogComponent>);
  readonly form;

  constructor(@Inject(MAT_DIALOG_DATA) public readonly data: PayRecurringDialogData) {
    this.form = this.fb.nonNullable.group({
      amount: [this.data.notification.amount, Validators.min(0.01)],
      fromAccountId: [null as number | null, Validators.required],
      subCategoryId: [this.data.notification.subCategoryId, Validators.required]
    });
  }

  close(): void {
    this.dialogRef.close();
  }

  confirm(): void {
    if (this.form.invalid) return;
    const value = this.form.getRawValue();
    const payload: CreateTransactionRequest = {
      type: 'EXPENSE',
      amount: value.amount,
      fromAccountId: value.fromAccountId,
      subCategoryId: value.subCategoryId
    };
    this.dialogRef.close(payload);
  }
}
