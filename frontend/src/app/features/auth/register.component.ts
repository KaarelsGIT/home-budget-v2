import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { AuthService } from '../../core/services/auth.service';
import { UserService } from '../../core/services/user.service';
import { UserSummary } from '../../core/models/user.model';
import { I18nPipe } from '../../shared/pipes/i18n.pipe';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, MatButtonModule, MatCardModule, MatFormFieldModule, MatInputModule, MatSelectModule, I18nPipe],
  styles: `
    .auth-page { min-height: 100vh; display: grid; place-items: center; background: linear-gradient(135deg, #1f2937, #14532d, #f97316); padding: 24px; }
    .auth-card { width: min(460px, 100%); padding: 12px; border-radius: 28px; }
    form { display: grid; gap: 12px; }
  `,
  template: `
    <section class="auth-page">
      <mat-card class="auth-card">
        <mat-card-header><mat-card-title>{{ 'auth.register' | i18n }}</mat-card-title></mat-card-header>
        <mat-card-content>
          <form [formGroup]="form" (ngSubmit)="submit()">
            <mat-form-field><mat-label>{{ 'auth.username' | i18n }}</mat-label><input matInput formControlName="username" /></mat-form-field>
            <mat-form-field><mat-label>{{ 'auth.password' | i18n }}</mat-label><input matInput type="password" formControlName="password" /></mat-form-field>
            <mat-form-field>
              <mat-label>{{ 'auth.role' | i18n }}</mat-label>
              <mat-select formControlName="role">
                <mat-option value="PARENT">{{ 'auth.parent' | i18n }}</mat-option>
                <mat-option value="CHILD">{{ 'auth.child' | i18n }}</mat-option>
              </mat-select>
            </mat-form-field>
            @if (form.controls.role.value === 'CHILD') {
              <mat-form-field>
                <mat-label>{{ 'auth.parentUser' | i18n }}</mat-label>
                <mat-select formControlName="parentId">
                  @for (user of parents(); track user.id) {
                    <mat-option [value]="user.id">{{ user.username }}</mat-option>
                  }
                </mat-select>
              </mat-form-field>
            }
            <p>{{ 'auth.pendingInfo' | i18n }}</p>
            <button mat-flat-button type="submit">{{ 'auth.register' | i18n }}</button>
            <a mat-button routerLink="/auth/login">{{ 'auth.login' | i18n }}</a>
          </form>
        </mat-card-content>
      </mat-card>
    </section>
  `
})
export class RegisterComponent {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly userService = inject(UserService);

  readonly parents = signal<UserSummary[]>([]);
  readonly form = this.fb.group({
    username: ['', Validators.required],
    password: ['', Validators.required],
    role: ['PARENT' as 'PARENT' | 'CHILD', Validators.required],
    parentId: [null as number | null]
  });

  constructor() {
    this.userService.getUsers().subscribe((users) => this.parents.set(users.filter((user) => user.role === 'PARENT')));
  }

  submit(): void {
    if (this.form.invalid) return;
    const value = this.form.getRawValue();
    this.authService.register({
      username: value.username ?? '',
      password: value.password ?? '',
      role: value.role ?? 'PARENT',
      parentId: value.parentId
    }).subscribe();
  }
}
