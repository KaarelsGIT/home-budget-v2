import { Component, signal } from '@angular/core';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { forkJoin } from 'rxjs';
import { MatCardModule } from '@angular/material/card';
import { MatListModule } from '@angular/material/list';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { DashboardService } from '../../core/services/dashboard.service';
import { SignedAmountPipe } from '../../shared/pipes/signed-amount.pipe';
import { Transaction } from '../../core/models/transaction.model';
import { Notification } from '../../core/models/notification.model';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [MatCardModule, MatListModule, MatProgressSpinnerModule, CurrencyPipe, DatePipe, SignedAmountPipe],
  styles: `
    .grid {
      display: grid;
      grid-template-columns: repeat(12, 1fr);
      gap: 16px;
    }

    .metric {
      grid-column: span 12;
      background: linear-gradient(135deg, #0f60af, #4fa6ff);
      color: white;
    }

    .metric h2 {
      margin: 4px 0;
      font-size: 2rem;
    }

    .card {
      grid-column: span 6;
    }

    .muted {
      color: #5d6671;
    }

    @media (max-width: 900px) {
      .card {
        grid-column: span 12;
      }
    }
  `,
  template: `
    @if (loading()) {
      <mat-spinner diameter="44" />
    } @else {
      <section class="grid">
        <mat-card class="metric">
          <mat-card-header>
            <mat-card-subtitle>Total Balance</mat-card-subtitle>
            <mat-card-title>
              <h2>{{ totalBalance() | currency : 'EUR' }}</h2>
            </mat-card-title>
          </mat-card-header>
        </mat-card>

        <mat-card class="card">
          <mat-card-header>
            <mat-card-title>Recent Transactions</mat-card-title>
          </mat-card-header>
          <mat-card-content>
            <mat-list>
              @for (transaction of recentTransactions(); track transaction.id) {
                <mat-list-item>
                  <span matListItemTitle>{{ transaction.categoryName || transaction.type }}</span>
                  <span matListItemLine class="muted">{{ transaction.createdAt | date : 'short' }}</span>
                  <span matListItemMeta>
                    {{ transaction.amount | signedAmount : transaction.type }}
                  </span>
                </mat-list-item>
              } @empty {
                <p class="muted">No transactions yet.</p>
              }
            </mat-list>
          </mat-card-content>
        </mat-card>

        <mat-card class="card">
          <mat-card-header>
            <mat-card-title>Notifications</mat-card-title>
          </mat-card-header>
          <mat-card-content>
            <mat-list>
              @for (notification of notifications(); track notification.id) {
                <mat-list-item>
                  <span matListItemTitle>{{ notification.message }}</span>
                  <span matListItemLine class="muted">{{ notification.createdAt | date : 'short' }}</span>
                </mat-list-item>
              } @empty {
                <p class="muted">No notifications.</p>
              }
            </mat-list>
          </mat-card-content>
        </mat-card>
      </section>
    }
  `
})
export class DashboardComponent {
  readonly loading = signal(true);
  readonly totalBalance = signal(0);
  readonly recentTransactions = signal<Transaction[]>([]);
  readonly notifications = signal<Notification[]>([]);

  constructor(private readonly dashboardService: DashboardService) {
    forkJoin({
      summary: this.dashboardService.loadDashboard(),
      recentTransactions: this.dashboardService.getRecentTransactions(),
      notifications: this.dashboardService.getRecentNotifications()
    }).subscribe({
      next: ({ summary, recentTransactions, notifications }) => {
        this.totalBalance.set(summary.totalBalance);
        this.recentTransactions.set(recentTransactions);
        this.notifications.set(notifications);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
      }
    });
  }
}
