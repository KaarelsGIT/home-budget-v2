import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { AuthService } from '../../core/services/auth.service';
import { I18nPipe } from '../../shared/pipes/i18n.pipe';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, MatButtonModule, MatCardModule, MatFormFieldModule, MatInputModule, I18nPipe],
  styles: `
    .auth-page { min-height: 100vh; display: grid; place-items: center; background: linear-gradient(135deg, #0f172a, #1d4ed8, #f59e0b); padding: 24px; }
    .auth-card { width: min(420px, 100%); padding: 12px; border-radius: 28px; }
    form { display: grid; gap: 12px; }
  `,
  template: `
    <section class="auth-page">
      <mat-card class="auth-card">
        <mat-card-header><mat-card-title>{{ 'auth.login' | i18n }}</mat-card-title></mat-card-header>
        <mat-card-content>
          <form [formGroup]="form" (ngSubmit)="submit()">
            <mat-form-field><mat-label>{{ 'auth.username' | i18n }}</mat-label><input matInput formControlName="username" /></mat-form-field>
            <mat-form-field><mat-label>{{ 'auth.password' | i18n }}</mat-label><input matInput type="password" formControlName="password" /></mat-form-field>
            <button mat-flat-button type="submit">{{ 'auth.login' | i18n }}</button>
            <a mat-button routerLink="/auth/register">{{ 'auth.register' | i18n }}</a>
          </form>
        </mat-card-content>
      </mat-card>
    </section>
  `
})
export class LoginComponent {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  readonly form = this.fb.nonNullable.group({
    username: ['', Validators.required],
    password: ['', Validators.required]
  });

  submit(): void {
    if (this.form.invalid) return;
    this.authService.login(this.form.getRawValue()).subscribe(() => {
      void this.router.navigate(['/dashboard']);
    });
  }
}
