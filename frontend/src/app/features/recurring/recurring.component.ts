import { Component, inject, signal } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { forkJoin } from 'rxjs';
import { AccountService } from '../../core/services/account.service';
import { CategoryService } from '../../core/services/category.service';
import { RecurringService } from '../../core/services/recurring.service';
import { TransactionService } from '../../core/services/transaction.service';
import { Account } from '../../core/models/account.model';
import { RecurringPayment, RecurringPaymentNotification } from '../../core/models/recurring.model';
import { SubCategory } from '../../core/models/category.model';
import { PayRecurringDialogComponent } from './pay-recurring-dialog.component';
import { I18nPipe } from '../../shared/pipes/i18n.pipe';

@Component({
  selector: 'app-recurring',
  standalone: true,
  imports: [ReactiveFormsModule, CurrencyPipe, MatButtonModule, MatCardModule, MatDialogModule, MatFormFieldModule, MatInputModule, MatSelectModule, I18nPipe],
  template: `
    <mat-card>
      <mat-card-header><mat-card-title>{{ 'recurring.title' | i18n }}</mat-card-title></mat-card-header>
      <mat-card-content>
        <form [formGroup]="form" (ngSubmit)="create()" style="display:grid;grid-template-columns:repeat(5,minmax(0,1fr));gap:12px;margin-bottom:16px;">
          <mat-form-field><mat-label>{{ 'common.name' | i18n }}</mat-label><input matInput formControlName="name" /></mat-form-field>
          <mat-form-field><mat-label>{{ 'common.amount' | i18n }}</mat-label><input matInput type="number" formControlName="amount" /></mat-form-field>
          <mat-form-field><mat-label>{{ 'transactions.subCategory' | i18n }}</mat-label><mat-select formControlName="subCategoryId">@for (sub of subCategories(); track sub.id) { <mat-option [value]="sub.id">{{ sub.parentCategoryName }} / {{ sub.name }}</mat-option> }</mat-select></mat-form-field>
          <mat-form-field><mat-label>{{ 'common.dueDay' | i18n }}</mat-label><input matInput type="number" formControlName="dueDay" /></mat-form-field>
          <button mat-flat-button type="submit">{{ 'recurring.add' | i18n }}</button>
        </form>

        <h3>{{ 'recurring.notifications' | i18n }}</h3>
        @for (notification of notifications(); track notification.recurringPaymentId) {
          <mat-card style="margin-bottom:12px;">
            <mat-card-title>{{ notification.name }} · {{ notification.amount | currency:'EUR' }}</mat-card-title>
            <mat-card-subtitle>{{ notification.categoryName }} / {{ notification.subCategoryName }}</mat-card-subtitle>
            <mat-card-actions><button mat-flat-button (click)="pay(notification)">{{ 'common.pay' | i18n }}</button></mat-card-actions>
          </mat-card>
        } @empty { <p>{{ 'common.noData' | i18n }}</p> }

        <h3>{{ 'recurring.title' | i18n }}</h3>
        @for (payment of payments(); track payment.id) {
          <mat-card style="margin-bottom:12px;">
            <mat-card-title>{{ payment.name }} · {{ payment.amount | currency:'EUR' }}</mat-card-title>
            <mat-card-subtitle>{{ payment.ownerUsername }} · {{ payment.dueDay }}</mat-card-subtitle>
          </mat-card>
        }
      </mat-card-content>
    </mat-card>
  `
})
export class RecurringComponent {
  private readonly fb = inject(FormBuilder);
  private readonly recurringService = inject(RecurringService);
  private readonly categoryService = inject(CategoryService);
  private readonly accountService = inject(AccountService);
  private readonly transactionService = inject(TransactionService);
  private readonly dialog = inject(MatDialog);

  readonly payments = signal<RecurringPayment[]>([]);
  readonly notifications = signal<RecurringPaymentNotification[]>([]);
  readonly subCategories = signal<SubCategory[]>([]);
  readonly accounts = signal<Account[]>([]);
  readonly form = this.fb.nonNullable.group({
    name: ['', Validators.required],
    amount: [0, Validators.min(0.01)],
    subCategoryId: [0, Validators.required],
    dueDay: [1, Validators.min(1)]
  });

  constructor() {
    this.load();
  }

  create(): void {
    const value = this.form.getRawValue();
    this.recurringService.createRecurringPayment({
      name: value.name,
      amount: value.amount,
      subCategoryId: value.subCategoryId,
      dueDay: value.dueDay,
      active: true
    }).subscribe(() => {
      this.form.reset({ name: '', amount: 0, subCategoryId: 0, dueDay: 1 });
      this.load();
    });
  }

  pay(notification: RecurringPaymentNotification): void {
    const dialogRef = this.dialog.open(PayRecurringDialogComponent, {
      data: { notification, accounts: this.accounts(), subCategories: this.subCategories() }
    });
    dialogRef.afterClosed().subscribe((payload) => {
      if (!payload) return;
      this.transactionService.createTransaction(payload).subscribe((transaction) => {
        this.recurringService.markPaid(notification.recurringPaymentId, transaction.id).subscribe(() => this.load());
      });
    });
  }

  private load(): void {
    forkJoin({
      payments: this.recurringService.getRecurringPayments(),
      notifications: this.recurringService.getNotifications(),
      categories: this.categoryService.getCategories(),
      accounts: this.accountService.getAccounts()
    }).subscribe(({ payments, notifications, categories, accounts }) => {
      this.payments.set(payments);
      this.notifications.set(notifications);
      this.accounts.set(accounts);
      this.subCategories.set(categories.flatMap((category) => category.subCategories));
    });
  }
}
