import { Component, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatListModule } from '@angular/material/list';
import { MatButtonModule } from '@angular/material/button';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { NotificationService } from '../../core/services/notification.service';
import { Notification } from '../../core/models/notification.model';

@Component({
  selector: 'app-notifications',
  standalone: true,
  imports: [MatCardModule, MatListModule, MatButtonModule, MatSnackBarModule, DatePipe],
  styles: `
    .row {
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 8px;
      width: 100%;
    }

    .muted {
      color: #637180;
    }

    .unread {
      font-weight: 700;
    }
  `,
  template: `
    <mat-card>
      <mat-card-header>
        <mat-card-title>Notifications</mat-card-title>
      </mat-card-header>
      <mat-card-content>
        <mat-list>
          @for (notification of notifications(); track notification.id) {
            <mat-list-item>
              <div class="row" [class.unread]="!notification.read">
                <div>
                  <div>{{ notification.message }}</div>
                  <div class="muted">{{ notification.createdAt | date : 'short' }}</div>
                </div>
                @if (!notification.read) {
                  <button mat-stroked-button color="primary" (click)="markRead(notification)">Mark as read</button>
                }
              </div>
            </mat-list-item>
          } @empty {
            <p class="muted">No notifications found.</p>
          }
        </mat-list>
      </mat-card-content>
    </mat-card>
  `
})
export class NotificationsComponent {
  readonly notifications = signal<Notification[]>([]);

  constructor(
    private readonly notificationService: NotificationService,
    private readonly snackBar: MatSnackBar
  ) {
    this.loadNotifications();
  }

  loadNotifications(): void {
    this.notificationService.getNotifications().subscribe((rows) => {
      const sortedRows = [...rows].sort((a, b) => b.createdAt.localeCompare(a.createdAt));
      this.notifications.set(sortedRows);
    });
  }

  markRead(notification: Notification): void {
    this.notificationService.markAsRead(notification.id).subscribe(() => {
      this.snackBar.open('Notification marked as read', 'Close', { duration: 2200 });
      this.loadNotifications();
    });
  }
}
