import { Component, computed, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { MatSelectModule } from '@angular/material/select';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatToolbarModule } from '@angular/material/toolbar';
import { AuthService } from '../../core/services/auth.service';
import { I18nService } from '../../core/services/i18n.service';
import { I18nPipe } from '../pipes/i18n.pipe';
import { LanguageCode } from '../../core/models/auth.model';

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
    MatIconModule,
    MatSelectModule,
    I18nPipe
  ],
  styles: `
    .shell { min-height: 100vh; background: radial-gradient(circle at top, #fef3c7, #f8fafc 42%, #e2e8f0); }
    .toolbar { position: sticky; top: 0; z-index: 20; display: flex; gap: 16px; justify-content: space-between; background: #111827; color: #fff; }
    .brand { font-weight: 800; letter-spacing: 0.08em; text-transform: uppercase; }
    .sidebar { width: 260px; padding: 16px 12px; background: linear-gradient(180deg, #fff7ed, #ffffff); border-right: 1px solid #e5e7eb; }
    .content { padding: 24px; max-width: 1280px; margin: 0 auto; }
    .nav-link { border-radius: 16px; margin-bottom: 6px; }
    .active { background: #f59e0b20; color: #92400e; }
    .toolbar-actions { display: flex; align-items: center; gap: 12px; }
    .user-chip { font-size: 0.95rem; opacity: 0.85; }
    @media (max-width: 900px) { .content { padding: 16px; } .sidebar { width: 220px; } }
  `,
  template: `
    <mat-sidenav-container class="shell">
      <mat-sidenav opened mode="side" class="sidebar">
        <div class="brand">{{ 'appName' | i18n }}</div>
        <mat-nav-list>
          @for (item of navItems(); track item.path) {
            <a mat-list-item class="nav-link" [routerLink]="item.path" routerLinkActive="active">{{ item.label | i18n }}</a>
          }
        </mat-nav-list>
      </mat-sidenav>

      <mat-sidenav-content>
        <mat-toolbar class="toolbar">
          <span>{{ 'appName' | i18n }}</span>
          <div class="toolbar-actions">
            <span class="user-chip">{{ authService.currentUser()?.username }}</span>
            <mat-select [value]="i18nService.language()" (valueChange)="changeLanguage($event)">
              @for (language of i18nService.languages; track language) {
                <mat-option [value]="language">{{ language.toUpperCase() }}</mat-option>
              }
            </mat-select>
            <button mat-flat-button (click)="logout()">{{ 'common.logout' | i18n }}</button>
          </div>
        </mat-toolbar>
        <main class="content">
          <router-outlet />
        </main>
      </mat-sidenav-content>
    </mat-sidenav-container>
  `
})
export class LayoutShellComponent {
  readonly authService = inject(AuthService);
  readonly i18nService = inject(I18nService);
  private readonly router = inject(Router);

  readonly navItems = computed(() => {
    const role = this.authService.currentUser()?.role;
    const items = [
      { path: '/dashboard', label: 'nav.dashboard' },
      { path: '/accounts', label: 'nav.accounts' },
      { path: '/transactions', label: 'nav.transactions' },
      { path: '/categories', label: 'nav.categories' },
      { path: '/recurring', label: 'nav.recurring' },
      { path: '/stats', label: 'nav.stats' }
    ];
    if (role === 'ADMIN') {
      items.push({ path: '/users', label: 'nav.users' });
    }
    return items;
  });

  async changeLanguage(language: LanguageCode): Promise<void> {
    await this.i18nService.setLanguage(language);
  }

  logout(): void {
    this.authService.logout();
    void this.router.navigate(['/auth/login']);
  }
}
