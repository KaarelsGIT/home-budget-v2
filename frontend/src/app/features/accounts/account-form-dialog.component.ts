import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogActions, MatDialogClose, MatDialogContent, MatDialogRef, MatDialogTitle } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';
import { Account, AccountRequest, AccountType } from '../../core/models/account.model';

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
    MatButtonModule,
    MatSelectModule
  ],
  template: `
    <h2 mat-dialog-title>{{ data.account ? 'Edit Account' : 'New Account' }}</h2>
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
          <mat-label>Type</mat-label>
          <mat-select formControlName="type">
            @for (type of accountTypes; track type) {
              <mat-option [value]="type">{{ type }}</mat-option>
            }
          </mat-select>
        </mat-form-field>

        <mat-form-field>
          <mat-label>Parent Account</mat-label>
          <mat-select formControlName="parentAccountId">
            <mat-option [value]="null">None</mat-option>
            @for (account of parentOptions(); track account.id) {
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
export class AccountFormDialogComponent {
  private readonly fb = inject(FormBuilder);
  private readonly dialogRef = inject(MatDialogRef<AccountFormDialogComponent>);
  readonly data = inject<{ account: Account | null; accounts: Account[] }>(MAT_DIALOG_DATA);
  readonly accountTypes: AccountType[] = ['PERSONAL', 'SHARED'];

  readonly form = this.fb.group({
    name: [this.data.account?.name ?? '', [Validators.required]],
    balance: [this.data.account?.balance ?? 0, [Validators.required, Validators.min(0)]],
    type: [this.data.account?.type ?? ('PERSONAL' as AccountType), [Validators.required]],
    parentAccountId: [this.data.account?.parentAccountId ?? null]
  });

  parentOptions(): Account[] {
    return this.data.accounts.filter((account) => account.id !== this.data.account?.id);
  }

  save(): void {
    if (this.form.invalid) {
      return;
    }

    this.dialogRef.close(this.form.getRawValue() as AccountRequest);
  }
}
