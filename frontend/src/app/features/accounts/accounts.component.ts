import { Component, signal } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatCardModule } from '@angular/material/card';
import { AccountService } from '../../core/services/account.service';
import { Account, AccountRequest } from '../../core/models/account.model';
import { ConfirmDialogComponent } from '../../shared/components/confirm-dialog.component';
import { AccountFormDialogComponent } from './account-form-dialog.component';

@Component({
  selector: 'app-accounts',
  standalone: true,
  imports: [
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatDialogModule,
    MatSnackBarModule,
    MatCardModule,
    CurrencyPipe
  ],
  template: `
    <mat-card>
      <mat-card-header>
        <mat-card-title>Accounts</mat-card-title>
        <button mat-flat-button color="primary" (click)="openCreateDialog()">Add Account</button>
      </mat-card-header>
      <mat-card-content>
        <table mat-table [dataSource]="accounts()" class="full-width">
          <ng-container matColumnDef="name">
            <th mat-header-cell *matHeaderCellDef>Name</th>
            <td mat-cell *matCellDef="let row">{{ row.name }}</td>
          </ng-container>

          <ng-container matColumnDef="balance">
            <th mat-header-cell *matHeaderCellDef>Balance</th>
            <td mat-cell *matCellDef="let row">{{ row.balance | currency : 'EUR' }}</td>
          </ng-container>

          <ng-container matColumnDef="type">
            <th mat-header-cell *matHeaderCellDef>Type</th>
            <td mat-cell *matCellDef="let row">{{ row.type }}</td>
          </ng-container>

          <ng-container matColumnDef="actions">
            <th mat-header-cell *matHeaderCellDef></th>
            <td mat-cell *matCellDef="let row">
              <button mat-icon-button (click)="openEditDialog(row)">
                <mat-icon>edit</mat-icon>
              </button>
              <button mat-icon-button color="warn" (click)="deleteAccount(row)">
                <mat-icon>delete</mat-icon>
              </button>
            </td>
          </ng-container>

          <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
          <tr mat-row *matRowDef="let row; columns: displayedColumns"></tr>
        </table>
      </mat-card-content>
    </mat-card>
  `
})
export class AccountsComponent {
  readonly displayedColumns = ['name', 'balance', 'type', 'actions'];
  readonly accounts = signal<Account[]>([]);

  constructor(
    private readonly accountService: AccountService,
    private readonly dialog: MatDialog,
    private readonly snackBar: MatSnackBar
  ) {
    this.loadAccounts();
  }

  loadAccounts(): void {
    this.accountService.getAccounts().subscribe((accounts) => this.accounts.set(accounts));
  }

  openCreateDialog(): void {
    const ref = this.dialog.open(AccountFormDialogComponent, {
      data: { account: null, accounts: this.accounts() }
    });
    ref.afterClosed().subscribe((payload: AccountRequest | undefined) => {
      if (!payload) {
        return;
      }
      this.accountService.createAccount(payload).subscribe(() => {
        this.snackBar.open('Account created', 'Close', { duration: 2500 });
        this.loadAccounts();
      });
    });
  }

  openEditDialog(account: Account): void {
    const ref = this.dialog.open(AccountFormDialogComponent, {
      data: { account, accounts: this.accounts() }
    });
    ref.afterClosed().subscribe((payload: AccountRequest | undefined) => {
      if (!payload) {
        return;
      }
      this.accountService.updateAccount(account.id, payload).subscribe(() => {
        this.snackBar.open('Account updated', 'Close', { duration: 2500 });
        this.loadAccounts();
      });
    });
  }

  deleteAccount(account: Account): void {
    const ref = this.dialog.open(ConfirmDialogComponent, {
      data: { title: 'Delete Account', message: `Delete ${account.name}?` }
    });

    ref.afterClosed().subscribe((confirmed: boolean) => {
      if (!confirmed) {
        return;
      }

      this.accountService.deleteAccount(account.id).subscribe(() => {
        this.snackBar.open('Account deleted', 'Close', { duration: 2500 });
        this.loadAccounts();
      });
    });
  }
}
