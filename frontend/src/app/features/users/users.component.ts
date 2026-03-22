import { Component, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { UserService } from '../../core/services/user.service';
import { UserSummary } from '../../core/models/user.model';
import { I18nPipe } from '../../shared/pipes/i18n.pipe';

@Component({
  selector: 'app-users',
  standalone: true,
  imports: [MatButtonModule, MatCardModule, I18nPipe],
  template: `
    <mat-card>
      <mat-card-header><mat-card-title>{{ 'users.title' | i18n }}</mat-card-title></mat-card-header>
      <mat-card-content>
        @for (user of users(); track user.id) {
          <mat-card style="margin-bottom:12px;">
            <mat-card-title>{{ user.username }}</mat-card-title>
            <mat-card-subtitle>{{ user.role }} · {{ user.status }}</mat-card-subtitle>
            @if (user.status === 'PENDING') {
              <mat-card-actions><button mat-flat-button (click)="approve(user.id)">{{ 'users.approve' | i18n }}</button></mat-card-actions>
            }
          </mat-card>
        }
      </mat-card-content>
    </mat-card>
  `
})
export class UsersComponent {
  private readonly userService = inject(UserService);
  readonly users = signal<UserSummary[]>([]);

  constructor() {
    this.load();
  }

  approve(id: number): void {
    this.userService.approveUser(id).subscribe(() => this.load());
  }

  private load(): void {
    this.userService.getUsers().subscribe((users) => this.users.set(users));
  }
}
