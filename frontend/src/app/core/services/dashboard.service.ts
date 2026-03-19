import { Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { AccountService } from './account.service';
import { NotificationService } from './notification.service';
import { TransactionService } from './transaction.service';
import { Account } from '../models/account.model';
import { Notification } from '../models/notification.model';
import { Transaction } from '../models/transaction.model';

export interface DashboardData {
  totalBalance: number;
  accounts: Account[];
}

@Injectable({ providedIn: 'root' })
export class DashboardService {
  constructor(
    private readonly accountService: AccountService,
    private readonly transactionService: TransactionService,
    private readonly notificationService: NotificationService
  ) {}

  loadDashboard(): Observable<DashboardData> {
    return this.accountService.getAccounts().pipe(
      map((accounts) => ({
        accounts,
        totalBalance: accounts.reduce((acc, account) => acc + Number(account.balance), 0)
      })),
      map(({ accounts, totalBalance }) => ({ accounts, totalBalance }))
    );
  }

  getRecentTransactions(limit = 5): Observable<Transaction[]> {
    return this.transactionService
      .filterTransactions({ sortBy: 'date', direction: 'DESC' })
      .pipe(map((rows) => rows.slice(0, limit)));
  }

  getRecentNotifications(limit = 5): Observable<Notification[]> {
    return this.notificationService
      .getNotifications()
      .pipe(map((rows) => rows.sort((a, b) => b.createdAt.localeCompare(a.createdAt)).slice(0, limit)));
  }
}
