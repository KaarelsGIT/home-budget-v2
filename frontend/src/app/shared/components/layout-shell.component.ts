import { Component, computed, signal } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatListModule } from '@angular/material/list';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { BreakpointObserver } from '@angular/cdk/layout';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-layout-shell',
  standalone: true,
  imports: [
    RouterOutlet,
    RouterLink,
    RouterLinkActive,
    MatToolbarModule,
    MatSidenavModule,
    MatListModule,
    MatButtonModule,
    MatIconModule
  ],
  styles: `
    .shell {
      height: 100vh;
    }

    .toolbar {
      position: sticky;
      top: 0;
      z-index: 20;
      display: flex;
      justify-content: space-between;
      gap: 16px;
    }

    .content {
      padding: 20px;
      max-width: 1200px;
      margin: 0 auto;
    }

    .active-link {
      background: #d8ecff;
      border-radius: 12px;
    }

    .title {
      font-weight: 700;
      letter-spacing: 0.02em;
    }

    @media (max-width: 768px) {
      .content {
        padding: 12px;
      }
    }
  `,
  template: `
    <mat-toolbar color="primary" class="toolbar">
      <span class="title">Home Budget</span>
      <span>{{ userEmail() }}</span>
      <button mat-flat-button color="accent" (click)="logout()">Logout</button>
    </mat-toolbar>

    <mat-sidenav-container class="shell">
      <mat-sidenav [mode]="isMobile() ? 'over' : 'side'" [opened]="!isMobile()">
        <mat-nav-list>
          @for (item of navItems; track item.path) {
            <a mat-list-item [routerLink]="item.path" routerLinkActive="active-link">{{ item.label }}</a>
          }
        </mat-nav-list>
      </mat-sidenav>

      <mat-sidenav-content>
        <main class="content">
          <router-outlet />
        </main>
      </mat-sidenav-content>
    </mat-sidenav-container>
  `
})
export class LayoutShellComponent {
  readonly navItems = [
    { path: '/dashboard', label: 'Dashboard' },
    { path: '/accounts', label: 'Accounts' },
    { path: '/transactions', label: 'Transactions' },
    { path: '/categories', label: 'Categories' },
    { path: '/recurring', label: 'Recurring' },
    { path: '/notifications', label: 'Notifications' }
  ];

  private readonly isMobileSignal = signal(false);
  readonly isMobile = this.isMobileSignal.asReadonly();
  readonly userEmail = computed(() => this.authService.currentUser()?.email ?? '');

  constructor(
    private readonly authService: AuthService,
    private readonly router: Router,
    breakpointObserver: BreakpointObserver
  ) {
    breakpointObserver.observe('(max-width: 768px)').subscribe((state) => {
      this.isMobileSignal.set(state.matches);
    });
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/auth/login']);
  }
}
