import { Component, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { forkJoin } from 'rxjs';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { AccountService } from '../../core/services/account.service';
import { CategoryService } from '../../core/services/category.service';
import { RecurringService } from '../../core/services/recurring.service';
import { Account } from '../../core/models/account.model';
import { CategoryTreeNode } from '../../core/models/category.model';
import { RecurringTransaction, RecurringTransactionRequest } from '../../core/models/recurring.model';
import { ConfirmDialogComponent } from '../../shared/components/confirm-dialog.component';
import { RecurringFormDialogComponent } from './recurring-form-dialog.component';

@Component({
  selector: 'app-recurring',
  standalone: true,
  imports: [MatCardModule, MatTableModule, MatButtonModule, MatIconModule, MatDialogModule, MatSnackBarModule, DatePipe],
  template: `
    <mat-card>
      <mat-card-header>
        <mat-card-title>Recurring Payments</mat-card-title>
        <button mat-flat-button color="primary" (click)="openCreateDialog()">Add Recurring</button>
      </mat-card-header>
      <mat-card-content>
        <h3>Upcoming Payments</h3>
        <table mat-table [dataSource]="upcoming()" class="full-width">
          <ng-container matColumnDef="amount">
            <th mat-header-cell *matHeaderCellDef>Amount</th>
            <td mat-cell *matCellDef="let row">{{ row.amount }}</td>
          </ng-container>

          <ng-container matColumnDef="frequency">
            <th mat-header-cell *matHeaderCellDef>Frequency</th>
            <td mat-cell *matCellDef="let row">{{ row.frequency }}</td>
          </ng-container>

          <ng-container matColumnDef="nextExecutionDate">
            <th mat-header-cell *matHeaderCellDef>Next Date</th>
            <td mat-cell *matCellDef="let row">{{ row.nextExecutionDate | date }}</td>
          </ng-container>

          <ng-container matColumnDef="active">
            <th mat-header-cell *matHeaderCellDef>Active</th>
            <td mat-cell *matCellDef="let row">{{ row.active ? 'Yes' : 'No' }}</td>
          </ng-container>

          <ng-container matColumnDef="actions">
            <th mat-header-cell *matHeaderCellDef></th>
            <td mat-cell *matCellDef="let row">
              <button mat-icon-button (click)="openEditDialog(row)">
                <mat-icon>edit</mat-icon>
              </button>
              <button mat-icon-button color="warn" (click)="deleteRecurring(row)">
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
export class RecurringComponent {
  readonly displayedColumns = ['amount', 'frequency', 'nextExecutionDate', 'active', 'actions'];
  readonly recurring = signal<RecurringTransaction[]>([]);
  readonly upcoming = signal<RecurringTransaction[]>([]);
  readonly accounts = signal<Account[]>([]);
  readonly categories = signal<CategoryTreeNode[]>([]);

  constructor(
    private readonly recurringService: RecurringService,
    private readonly accountService: AccountService,
    private readonly categoryService: CategoryService,
    private readonly dialog: MatDialog,
    private readonly snackBar: MatSnackBar
  ) {
    this.loadData();
  }

  loadData(): void {
    forkJoin({
      recurring: this.recurringService.getRecurring(),
      upcoming: this.recurringService.getUpcomingRecurring(),
      accounts: this.accountService.getAccounts(),
      categories: this.categoryService.getCategoryTree()
    }).subscribe(({ recurring, upcoming, accounts, categories }) => {
      this.recurring.set(recurring);
      this.upcoming.set(upcoming);
      this.accounts.set(accounts);
      this.categories.set(categories);
    });
  }

  openCreateDialog(): void {
    const ref = this.dialog.open(RecurringFormDialogComponent, {
      data: { recurring: null, categories: this.categories(), accounts: this.accounts() }
    });

    ref.afterClosed().subscribe((payload: RecurringTransactionRequest | undefined) => {
      if (!payload) {
        return;
      }

      this.recurringService.createRecurring(payload).subscribe(() => {
        this.snackBar.open('Recurring transaction created', 'Close', { duration: 2500 });
        this.loadData();
      });
    });
  }

  openEditDialog(item: RecurringTransaction): void {
    const ref = this.dialog.open(RecurringFormDialogComponent, {
      data: { recurring: item, categories: this.categories(), accounts: this.accounts() }
    });

    ref.afterClosed().subscribe((payload: RecurringTransactionRequest | undefined) => {
      if (!payload) {
        return;
      }

      this.recurringService.updateRecurring(item.id, payload).subscribe(() => {
        this.snackBar.open('Recurring transaction updated', 'Close', { duration: 2500 });
        this.loadData();
      });
    });
  }

  deleteRecurring(item: RecurringTransaction): void {
    const ref = this.dialog.open(ConfirmDialogComponent, {
      data: { title: 'Delete Recurring', message: 'Delete this recurring transaction?' }
    });

    ref.afterClosed().subscribe((confirmed: boolean) => {
      if (!confirmed) {
        return;
      }

      this.recurringService.deleteRecurring(item.id).subscribe(() => {
        this.snackBar.open('Recurring transaction deleted', 'Close', { duration: 2500 });
        this.loadData();
      });
    });
  }
}
