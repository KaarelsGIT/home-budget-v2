import { Component, inject, signal } from '@angular/core';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { forkJoin } from 'rxjs';
import { Account } from '../../core/models/account.model';
import { Category, SubCategory } from '../../core/models/category.model';
import { Transaction, TransactionType } from '../../core/models/transaction.model';
import { AccountService } from '../../core/services/account.service';
import { CategoryService } from '../../core/services/category.service';
import { TransactionService } from '../../core/services/transaction.service';
import { I18nPipe } from '../../shared/pipes/i18n.pipe';

@Component({
  selector: 'app-transactions',
  standalone: true,
  imports: [ReactiveFormsModule, CurrencyPipe, DatePipe, MatButtonModule, MatCardModule, MatFormFieldModule, MatInputModule, MatSelectModule, I18nPipe],
  template: `
    <mat-card>
      <mat-card-header><mat-card-title>{{ 'transactions.title' | i18n }}</mat-card-title></mat-card-header>
      <mat-card-content>
        <form [formGroup]="form" (ngSubmit)="create()" style="display:grid;grid-template-columns:repeat(5,minmax(0,1fr));gap:12px;margin-bottom:16px;">
          <mat-form-field>
            <mat-label>{{ 'common.type' | i18n }}</mat-label>
            <mat-select formControlName="type">
              <mat-option value="INCOME">{{ 'transactions.income' | i18n }}</mat-option>
              <mat-option value="EXPENSE">{{ 'transactions.expense' | i18n }}</mat-option>
              <mat-option value="TRANSFER">{{ 'transactions.transfer' | i18n }}</mat-option>
            </mat-select>
          </mat-form-field>
          <mat-form-field><mat-label>{{ 'common.amount' | i18n }}</mat-label><input matInput type="number" formControlName="amount" /></mat-form-field>
          <mat-form-field>
            <mat-label>{{ 'transactions.sourceAccount' | i18n }}</mat-label>
            <mat-select formControlName="fromAccountId">
              @for (account of accounts(); track account.id) { <mat-option [value]="account.id">{{ account.name }}</mat-option> }
            </mat-select>
          </mat-form-field>
          <mat-form-field>
            <mat-label>{{ 'transactions.destinationAccount' | i18n }}</mat-label>
            <mat-select formControlName="toAccountId">
              @for (account of accounts(); track account.id) { <mat-option [value]="account.id">{{ account.name }}</mat-option> }
            </mat-select>
          </mat-form-field>
          <mat-form-field>
            <mat-label>{{ 'transactions.subCategory' | i18n }}</mat-label>
            <mat-select formControlName="subCategoryId">
              @for (sub of subCategories(); track sub.id) { <mat-option [value]="sub.id">{{ sub.parentCategoryName }} / {{ sub.name }}</mat-option> }
            </mat-select>
          </mat-form-field>
          <button mat-flat-button type="submit">{{ 'transactions.create' | i18n }}</button>
        </form>

        @for (transaction of transactions(); track transaction.id) {
          <mat-card style="margin-bottom:12px;">
            <mat-card-title>{{ transaction.type }} · {{ transaction.amount | currency:'EUR' }}</mat-card-title>
            <mat-card-subtitle>{{ transaction.createdAt | date:'short' }} · {{ transaction.createdByUsername }}</mat-card-subtitle>
            <mat-card-content>{{ transaction.categoryName }} / {{ transaction.subCategoryName }}</mat-card-content>
            <mat-card-actions><button mat-button (click)="remove(transaction.id)">{{ 'common.delete' | i18n }}</button></mat-card-actions>
          </mat-card>
        }
      </mat-card-content>
    </mat-card>
  `
})
export class TransactionsComponent {
  private readonly fb = inject(FormBuilder);
  private readonly transactionService = inject(TransactionService);
  private readonly accountService = inject(AccountService);
  private readonly categoryService = inject(CategoryService);

  readonly accounts = signal<Account[]>([]);
  readonly transactions = signal<Transaction[]>([]);
  readonly categories = signal<Category[]>([]);
  readonly subCategories = signal<SubCategory[]>([]);
  readonly form = this.fb.nonNullable.group({
    type: ['EXPENSE' as TransactionType, Validators.required],
    amount: [0, Validators.min(0.01)],
    fromAccountId: [null as number | null],
    toAccountId: [null as number | null],
    subCategoryId: [null as number | null]
  });

  constructor() {
    this.load();
  }

  create(): void {
    const value = this.form.getRawValue();
    this.transactionService.createTransaction(value).subscribe(() => {
      this.form.reset({ type: 'EXPENSE', amount: 0, fromAccountId: null, toAccountId: null, subCategoryId: null });
      this.load();
    });
  }

  remove(id: number): void {
    this.transactionService.deleteTransaction(id).subscribe(() => this.load());
  }

  private load(): void {
    forkJoin({
      accounts: this.accountService.getAccounts(),
      transactions: this.transactionService.getTransactions(),
      categories: this.categoryService.getCategories()
    }).subscribe(({ accounts, transactions, categories }) => {
      this.accounts.set(accounts);
      this.transactions.set(transactions);
      this.categories.set(categories);
      this.subCategories.set(categories.flatMap((category) => category.subCategories));
    });
  }
}
