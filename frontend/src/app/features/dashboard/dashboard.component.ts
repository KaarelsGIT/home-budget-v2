import { Component, inject, signal } from '@angular/core';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { forkJoin } from 'rxjs';
import { MatCardModule } from '@angular/material/card';
import { MatListModule } from '@angular/material/list';
import { AccountService } from '../../core/services/account.service';
import { RecurringService } from '../../core/services/recurring.service';
import { TransactionService } from '../../core/services/transaction.service';
import { Account } from '../../core/models/account.model';
import { RecurringPaymentNotification } from '../../core/models/recurring.model';
import { Transaction } from '../../core/models/transaction.model';
import { I18nPipe } from '../../shared/pipes/i18n.pipe';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [MatCardModule, MatListModule, CurrencyPipe, DatePipe, I18nPipe],
  styles: `
    .grid { display: grid; gap: 18px; grid-template-columns: repeat(12, 1fr); }
    .hero { grid-column: span 12; background: linear-gradient(135deg, #111827, #1d4ed8); color: white; }
    .card { grid-column: span 6; }
    @media (max-width: 900px) { .card { grid-column: span 12; } }
  `,
  template: `
    <section class="grid">
      <mat-card class="hero">
        <mat-card-header><mat-card-title>{{ 'dashboard.totalBalance' | i18n }}</mat-card-title></mat-card-header>
        <mat-card-content><h1>{{ totalBalance() | currency:'EUR' }}</h1></mat-card-content>
      </mat-card>

      <mat-card class="card">
        <mat-card-header><mat-card-title>{{ 'dashboard.duePayments' | i18n }}</mat-card-title></mat-card-header>
        <mat-card-content>
          <mat-list>
            @for (item of notifications(); track item.recurringPaymentId) {
              <mat-list-item>{{ item.name }} · {{ item.amount | currency:'EUR' }}</mat-list-item>
            } @empty { <p>{{ 'common.noData' | i18n }}</p> }
          </mat-list>
        </mat-card-content>
      </mat-card>

      <mat-card class="card">
        <mat-card-header><mat-card-title>{{ 'dashboard.recentTransactions' | i18n }}</mat-card-title></mat-card-header>
        <mat-card-content>
          <mat-list>
            @for (item of transactions(); track item.id) {
              <mat-list-item>
                {{ item.categoryName || item.type }} · {{ item.amount | currency:'EUR' }} · {{ item.createdAt | date:'short' }}
              </mat-list-item>
            } @empty { <p>{{ 'common.noData' | i18n }}</p> }
          </mat-list>
        </mat-card-content>
      </mat-card>
    </section>
  `
})
export class DashboardComponent {
  private readonly accountService = inject(AccountService);
  private readonly recurringService = inject(RecurringService);
  private readonly transactionService = inject(TransactionService);

  readonly totalBalance = signal(0);
  readonly notifications = signal<RecurringPaymentNotification[]>([]);
  readonly transactions = signal<Transaction[]>([]);

  constructor() {
    forkJoin({
      accounts: this.accountService.getAccounts(),
      notifications: this.recurringService.getNotifications(),
      transactions: this.transactionService.getTransactions()
    }).subscribe(({ accounts, notifications, transactions }) => {
      this.totalBalance.set(accounts.reduce((sum: number, account: Account) => sum + Number(account.balance), 0));
      this.notifications.set(notifications);
      this.transactions.set(transactions.slice(0, 6));
    });
  }
}
