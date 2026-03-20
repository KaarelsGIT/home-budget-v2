import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogActions, MatDialogClose, MatDialogContent, MatDialogRef, MatDialogTitle } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { Account } from '../../core/models/account.model';
import { Category } from '../../core/models/category.model';
import { RecurringFrequency, RecurringTransaction, RecurringTransactionRequest } from '../../core/models/recurring.model';
import { CategorySelectComponent } from '../../shared/components/category-select.component';

export interface RecurringDialogData {
  recurring: RecurringTransaction | null;
  categories: Category[];
  accounts: Account[];
}

@Component({
  selector: 'app-recurring-form-dialog',
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
    <h2 mat-dialog-title>{{ data.recurring ? 'Edit Recurring' : 'New Recurring' }}</h2>
    <mat-dialog-content>
      <form [formGroup]="form" class="page-container">
        <mat-form-field>
          <mat-label>Amount</mat-label>
          <input matInput type="number" formControlName="amount" />
        </mat-form-field>

        <app-category-select
          [categories]="data.categories"
          [value]="form.get('categoryId')?.value ?? null"
          (valueChange)="form.get('categoryId')?.setValue($event)"
        />

        <mat-form-field>
          <mat-label>Frequency</mat-label>
          <mat-select formControlName="frequency">
            @for (frequency of frequencies; track frequency) {
              <mat-option [value]="frequency">{{ frequency }}</mat-option>
            }
          </mat-select>
        </mat-form-field>

        <mat-form-field>
          <mat-label>Next Execution Date</mat-label>
          <input matInput type="date" formControlName="nextExecutionDate" />
        </mat-form-field>

        <mat-form-field>
          <mat-label>Account</mat-label>
          <mat-select formControlName="accountId">
            <mat-option [value]="null">None</mat-option>
            @for (account of data.accounts; track account.id) {
              <mat-option [value]="account.id">{{ account.name }}</mat-option>
            }
          </mat-select>
        </mat-form-field>

        <mat-slide-toggle formControlName="active">Active</mat-slide-toggle>
      </form>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close="">Cancel</button>
      <button mat-flat-button color="primary" (click)="save()" [disabled]="form.invalid">Save</button>
    </mat-dialog-actions>
  `
})
export class RecurringFormDialogComponent {
  private readonly fb = inject(FormBuilder);
  private readonly dialogRef = inject(MatDialogRef<RecurringFormDialogComponent>);
  readonly data = inject<RecurringDialogData>(MAT_DIALOG_DATA);

  readonly frequencies: RecurringFrequency[] = ['DAILY', 'WEEKLY', 'MONTHLY'];

  readonly form = this.fb.group({
    amount: [this.data.recurring?.amount ?? 0, [Validators.required, Validators.min(0.01)]],
    categoryId: [this.data.recurring?.categoryId ?? null],
    frequency: [this.data.recurring?.frequency ?? ('MONTHLY' as RecurringFrequency), [Validators.required]],
    nextExecutionDate: [this.data.recurring?.nextExecutionDate ?? new Date().toISOString().slice(0, 10), [Validators.required]],
    active: [this.data.recurring?.active ?? true, [Validators.required]],
    accountId: [this.data.recurring?.accountId ?? null]
  });

  save(): void {
    if (this.form.invalid) {
      return;
    }

    const value = this.form.getRawValue();
    const payload: RecurringTransactionRequest = {
      amount: Number(value.amount ?? 0),
      categoryId: value.categoryId ?? null,
      frequency: (value.frequency ?? 'MONTHLY') as RecurringFrequency,
      nextExecutionDate: value.nextExecutionDate ?? new Date().toISOString().slice(0, 10),
      active: value.active ?? true,
      accountId: value.accountId ?? null
    };

    this.dialogRef.close(payload);
  }
}
