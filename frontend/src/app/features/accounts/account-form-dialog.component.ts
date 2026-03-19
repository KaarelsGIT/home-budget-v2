import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogActions, MatDialogClose, MatDialogContent, MatDialogRef, MatDialogTitle } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { Account } from '../../core/models/account.model';

@Component({
  selector: 'app-account-form-dialog',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatDialogTitle,
    MatDialogContent,
    MatDialogActions,
    MatDialogClose,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule
  ],
  template: `
    <h2 mat-dialog-title>{{ data ? 'Edit Account' : 'New Account' }}</h2>
    <mat-dialog-content>
      <form [formGroup]="form" class="page-container">
        <mat-form-field>
          <mat-label>Name</mat-label>
          <input matInput formControlName="name" />
        </mat-form-field>

        <mat-form-field>
          <mat-label>Balance</mat-label>
          <input matInput type="number" formControlName="balance" />
        </mat-form-field>

        <mat-form-field>
          <mat-label>Currency (3 letters)</mat-label>
          <input matInput formControlName="currency" />
        </mat-form-field>
      </form>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close="">Cancel</button>
      <button mat-flat-button color="primary" (click)="save()" [disabled]="form.invalid">Save</button>
    </mat-dialog-actions>
  `
})
export class AccountFormDialogComponent {
  private readonly fb = inject(FormBuilder);
  private readonly dialogRef = inject(MatDialogRef<AccountFormDialogComponent>);
  readonly data = inject<Account | null>(MAT_DIALOG_DATA);

  readonly form = this.fb.nonNullable.group({
    name: [this.data?.name ?? '', [Validators.required]],
    balance: [this.data?.balance ?? 0, [Validators.required, Validators.min(0)]],
    currency: [this.data?.currency ?? 'EUR', [Validators.required, Validators.minLength(3), Validators.maxLength(3)]]
  });

  save(): void {
    if (this.form.invalid) {
      return;
    }

    const value = this.form.getRawValue();
    this.dialogRef.close({
      ...value,
      currency: value.currency.toUpperCase()
    });
  }
}
