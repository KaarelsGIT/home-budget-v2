import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatSnackBarModule
  ],
  styles: `
    .auth-wrapper {
      min-height: 100vh;
      display: grid;
      place-items: center;
      padding: 16px;
    }

    .auth-card {
      width: min(460px, 100%);
      border-radius: 20px;
    }

    .auth-title {
      margin-bottom: 8px;
      font-size: 1.6rem;
      font-weight: 700;
    }

    .actions {
      display: flex;
      justify-content: end;
      margin-top: 8px;
    }
  `,
  template: `
    <div class="auth-wrapper">
      <mat-card class="auth-card">
        <mat-card-header>
          <mat-card-title class="auth-title">Sign in</mat-card-title>
        </mat-card-header>
        <mat-card-content>
          <form [formGroup]="form" (ngSubmit)="submit()" class="page-container">
            <mat-form-field>
              <mat-label>Email</mat-label>
              <input matInput type="email" formControlName="email" />
            </mat-form-field>

            <mat-form-field>
              <mat-label>Password</mat-label>
              <input matInput type="password" formControlName="password" />
            </mat-form-field>

            <div class="actions">
              <button type="submit" mat-flat-button color="primary" [disabled]="loading() || form.invalid">
                {{ loading() ? 'Signing in...' : 'Login' }}
              </button>
            </div>
          </form>
        </mat-card-content>
        <mat-card-actions>
          <a mat-button routerLink="/auth/register">Need an account? Register</a>
        </mat-card-actions>
      </mat-card>
    </div>
  `
})
export class LoginComponent {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly snackBar = inject(MatSnackBar);

  readonly loading = signal(false);

  readonly form = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required]]
  });

  submit(): void {
    if (this.form.invalid || this.loading()) {
      return;
    }

    this.loading.set(true);
    this.authService.login(this.form.getRawValue()).subscribe({
      next: () => {
        this.loading.set(false);
        this.snackBar.open('Welcome back', 'Close', { duration: 2500 });
        this.router.navigate(['/dashboard']);
      },
      error: (error) => {
        this.loading.set(false);
        this.snackBar.open(error?.error?.message ?? 'Login failed', 'Close', { duration: 3500 });
      }
    });
  }
}
