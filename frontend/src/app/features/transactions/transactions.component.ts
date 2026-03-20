import { Component, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatTooltipModule } from '@angular/material/tooltip';
import { TransactionService } from '../../core/services/transaction.service';
import { AccountService } from '../../core/services/account.service';
import { CategoryService } from '../../core/services/category.service';
import { Account } from '../../core/models/account.model';
import { Category } from '../../core/models/category.model';
import { Transaction, TransactionRequest, TransactionType } from '../../core/models/transaction.model';
import { SignedAmountPipe } from '../../shared/pipes/signed-amount.pipe';
import { ConfirmDialogComponent } from '../../shared/components/confirm-dialog.component';
import { TransactionFormDialogComponent } from './transaction-form-dialog.component';

@Component({
  selector: 'app-transactions',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatCardModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatDialogModule,
    MatSnackBarModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatTooltipModule,
    DatePipe,
    SignedAmountPipe
  ],
  styles: `
    .toolbar {
      display: grid;
      grid-template-columns: repeat(6, minmax(120px, 1fr));
      gap: 12px;
      margin-bottom: 16px;
    }

    .controls {
      display: flex;
      gap: 8px;
      margin-bottom: 16px;
      flex-wrap: wrap;
    }

    @media (max-width: 980px) {
      .toolbar {
        grid-template-columns: repeat(2, minmax(120px, 1fr));
      }
    }
  `,
  template: `
    <mat-card>
      <mat-card-header>
        <mat-card-title>Transactions</mat-card-title>
      </mat-card-header>
      <mat-card-content>
        <form [formGroup]="filterForm" class="toolbar">
          <mat-form-field>
            <mat-label>Start Date</mat-label>
            <input matInput type="date" formControlName="startDate" />
          </mat-form-field>

          <mat-form-field>
            <mat-label>End Date</mat-label>
            <input matInput type="date" formControlName="endDate" />
          </mat-form-field>

          <mat-form-field>
            <mat-label>Type</mat-label>
            <mat-select formControlName="type" (valueChange)="onFilterTypeChange($event)">
              <mat-option [value]="null">All</mat-option>
              @for (type of types; track type) {
                <mat-option [value]="type">{{ type }}</mat-option>
              }
            </mat-select>
          </mat-form-field>

          <mat-form-field>
            <mat-label>Category</mat-label>
            <mat-select formControlName="categoryId">
              <mat-option [value]="null">All</mat-option>
              @for (category of filterCategories(); track category.id) {
                <mat-option [value]="category.id">{{ category.name }}</mat-option>
              }
            </mat-select>
          </mat-form-field>

          <mat-form-field>
            <mat-label>Account</mat-label>
            <mat-select formControlName="accountId">
              <mat-option [value]="null">All</mat-option>
              @for (account of accounts(); track account.id) {
                <mat-option [value]="account.id">{{ account.name }}</mat-option>
              }
            </mat-select>
          </mat-form-field>

          <mat-form-field>
            <mat-label>Sort</mat-label>
            <mat-select formControlName="sortBy">
              <mat-option value="date">Date</mat-option>
              <mat-option value="amount">Amount</mat-option>
            </mat-select>
          </mat-form-field>
        </form>

        <div class="controls">
          <button mat-flat-button color="primary" (click)="applyFilters()">Apply Filters</button>
          <button mat-button (click)="resetFilters()">Reset</button>
          <button mat-flat-button color="accent" (click)="openCreateDialog()">Add Transaction</button>
        </div>

        <table mat-table [dataSource]="transactions()" class="full-width">
          <ng-container matColumnDef="date">
            <th mat-header-cell *matHeaderCellDef>Date</th>
            <td mat-cell *matCellDef="let row">{{ row.date | date }}</td>
          </ng-container>

          <ng-container matColumnDef="type">
            <th mat-header-cell *matHeaderCellDef>Type</th>
            <td mat-cell *matCellDef="let row">{{ row.type }}</td>
          </ng-container>

          <ng-container matColumnDef="category">
            <th mat-header-cell *matHeaderCellDef>Category</th>
            <td mat-cell *matCellDef="let row">{{ row.categoryName || '-' }}</td>
          </ng-container>

          <ng-container matColumnDef="amount">
            <th mat-header-cell *matHeaderCellDef>Amount</th>
            <td mat-cell *matCellDef="let row">{{ row.amount | signedAmount : row.type }}</td>
          </ng-container>

          <ng-container matColumnDef="description">
            <th mat-header-cell *matHeaderCellDef>Description</th>
            <td mat-cell *matCellDef="let row">{{ row.description || '-' }}</td>
          </ng-container>

          <ng-container matColumnDef="actions">
            <th mat-header-cell *matHeaderCellDef></th>
            <td mat-cell *matCellDef="let row">
              <button mat-icon-button matTooltip="Edit" aria-label="Edit transaction" (click)="openEditDialog(row)">
                <mat-icon>edit</mat-icon>
              </button>
              <button mat-icon-button color="warn" matTooltip="Delete" aria-label="Delete transaction" (click)="deleteTransaction(row.id)">
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
export class TransactionsComponent {
  private readonly fb = inject(FormBuilder);
  private readonly transactionService = inject(TransactionService);
  private readonly accountService = inject(AccountService);
  private readonly categoryService = inject(CategoryService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);

  readonly displayedColumns = ['date', 'type', 'category', 'amount', 'description', 'actions'];
  readonly types: TransactionType[] = ['INCOME', 'EXPENSE', 'TRANSFER'];
  readonly transactions = signal<Transaction[]>([]);
  readonly accounts = signal<Account[]>([]);
  readonly filterCategories = signal<Category[]>([]);

  readonly filterForm = this.fb.group({
    startDate: [''],
    endDate: [''],
    type: [null as TransactionType | null],
    categoryId: [null as number | null],
    accountId: [null as number | null],
    sortBy: ['date' as 'date' | 'amount'],
    direction: ['DESC' as 'ASC' | 'DESC']
  });

  constructor() {
    this.loadDependencies();
  }

  private loadDependencies(): void {
    forkJoin({
      transactions: this.transactionService.getTransactions(),
      accounts: this.accountService.getAccounts(),
      incomeCategories: this.categoryService.getCategoriesByType('INCOME'),
      expenseCategories: this.categoryService.getCategoriesByType('EXPENSE')
    }).subscribe(({ transactions, accounts, incomeCategories, expenseCategories }) => {
      this.transactions.set(transactions);
      this.accounts.set(accounts);
      this.filterCategories.set([...incomeCategories, ...expenseCategories]);
    });
  }

  onFilterTypeChange(type: TransactionType | null): void {
    this.filterForm.patchValue({ categoryId: null });

    if (!type || type === 'TRANSFER') {
      this.categoryService.getCategories().subscribe((categories) => this.filterCategories.set(categories));
      return;
    }

    const categoryType = type === 'INCOME' ? 'INCOME' : 'EXPENSE';
    this.categoryService.getCategoriesByType(categoryType).subscribe((categories) => this.filterCategories.set(categories));
  }

  applyFilters(): void {
    const value = this.filterForm.getRawValue();

    this.transactionService
      .filterTransactions({
        startDate: value.startDate || undefined,
        endDate: value.endDate || undefined,
        type: value.type ?? undefined,
        categoryId: value.categoryId ?? undefined,
        accountId: value.accountId ?? undefined,
        sortBy: value.sortBy ?? 'date',
        direction: value.direction ?? 'DESC'
      })
      .subscribe((rows) => this.transactions.set(rows));
  }

  resetFilters(): void {
    this.filterForm.reset({
      startDate: '',
      endDate: '',
      type: null,
      categoryId: null,
      accountId: null,
      sortBy: 'date',
      direction: 'DESC'
    });

    this.loadDependencies();
  }

  openCreateDialog(): void {
    const ref = this.dialog.open(TransactionFormDialogComponent, {
      data: {
        transaction: null,
        accounts: this.accounts()
      },
      width: '620px'
    });

    ref.afterClosed().subscribe((payload: TransactionRequest | undefined) => {
      if (!payload) {
        return;
      }

      this.transactionService.createTransaction(payload).subscribe(() => {
        this.snackBar.open('Transaction created', 'Close', { duration: 2500 });
        this.loadDependencies();
      });
    });
  }

  openEditDialog(transaction: Transaction): void {
    const ref = this.dialog.open(TransactionFormDialogComponent, {
      data: {
        transaction,
        accounts: this.accounts()
      },
      width: '620px'
    });

    ref.afterClosed().subscribe((payload: TransactionRequest | undefined) => {
      if (!payload) {
        return;
      }

      this.transactionService.updateTransaction(transaction.id, payload).subscribe(() => {
        this.snackBar.open('Transaction updated', 'Close', { duration: 2500 });
        this.loadDependencies();
      });
    });
  }

  deleteTransaction(transactionId: number): void {
    const ref = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Delete Transaction',
        message: `Delete transaction #${transactionId}?`
      }
    });

    ref.afterClosed().subscribe((confirmed: boolean) => {
      if (!confirmed) {
        return;
      }

      this.transactionService.deleteTransaction(transactionId).subscribe(() => {
        this.snackBar.open('Transaction deleted', 'Close', { duration: 2500 });
        this.loadDependencies();
      });
    });
  }
}
