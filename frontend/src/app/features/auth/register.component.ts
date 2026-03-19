import { Component, inject, signal } from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  ReactiveFormsModule,
  ValidationErrors,
  ValidatorFn,
  Validators
} from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { AuthService } from '../../core/services/auth.service';
import { UserRole } from '../../core/models/auth.model';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatSelectModule,
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
      width: min(520px, 100%);
      border-radius: 20px;
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
          <mat-card-title>Create account</mat-card-title>
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

            <mat-form-field>
              <mat-label>Role</mat-label>
              <mat-select formControlName="role">
                @for (role of roles; track role) {
                  <mat-option [value]="role">{{ role }}</mat-option>
                }
              </mat-select>
            </mat-form-field>

            <mat-form-field>
              <mat-label>Parent ID (optional)</mat-label>
              <input matInput type="number" formControlName="parentId" />
            </mat-form-field>

            <div class="actions">
              <button type="submit" mat-flat-button color="primary" [disabled]="loading() || form.invalid">
                {{ loading() ? 'Creating...' : 'Register' }}
              </button>
            </div>
          </form>
        </mat-card-content>
        <mat-card-actions>
          <a mat-button routerLink="/auth/login">Already have an account? Login</a>
        </mat-card-actions>
      </mat-card>
    </div>
  `
})
export class RegisterComponent {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly snackBar = inject(MatSnackBar);

  readonly loading = signal(false);
  readonly roles: UserRole[] = ['PARENT', 'CHILD'];

  readonly form = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required]],
    role: ['PARENT' as UserRole, [Validators.required]],
    parentId: [null as number | null]
  }, { validators: this.childParentValidator() });

  submit(): void {
    if (this.form.invalid || this.loading()) {
      return;
    }

    this.loading.set(true);
    const payload = this.form.getRawValue();

    this.authService
      .register({
        email: payload.email ?? '',
        password: payload.password ?? '',
        role: (payload.role ?? 'PARENT') as UserRole,
        parentId: payload.parentId ? Number(payload.parentId) : null
      })
      .subscribe({
        next: () => {
          this.loading.set(false);
          this.snackBar.open('Account created successfully', 'Close', { duration: 2500 });
          this.router.navigate(['/dashboard']);
        },
        error: (error) => {
          this.loading.set(false);
          this.snackBar.open(this.extractErrorMessage(error), 'Close', { duration: 4500 });
        }
      });
  }

  private childParentValidator(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      const role = control.get('role')?.value as UserRole | null;
      const parentId = control.get('parentId')?.value;

      if (role === 'CHILD' && (parentId === null || parentId === undefined || parentId === '')) {
        return { parentIdRequiredForChild: true };
      }

      return null;
    };
  }

  private extractErrorMessage(error: unknown): string {
    const httpError = error as {
      status?: number;
      error?: unknown;
      message?: string;
    };

    if (httpError?.status === 0) {
      return 'Cannot reach backend API (or blocked by CORS). Check backend is running on http://localhost:8080.';
    }

    if (typeof httpError?.error === 'string') {
      return httpError.error;
    }

    if (httpError?.error && typeof httpError.error === 'object') {
      const payload = httpError.error as Record<string, unknown>;

      if (typeof payload['error'] === 'string') {
        return payload['error'];
      }

      // Validation payload style: { field: "message" }
      const firstValidationMessage = Object.values(payload).find((value) => typeof value === 'string');
      if (typeof firstValidationMessage === 'string') {
        return firstValidationMessage;
      }
    }

    if (httpError?.message) {
      return httpError.message;
    }

    return 'Registration failed';
  }
}
