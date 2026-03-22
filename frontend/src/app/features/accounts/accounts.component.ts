import { Component, inject, signal } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { AccountService } from '../../core/services/account.service';
import { Account } from '../../core/models/account.model';
import { I18nPipe } from '../../shared/pipes/i18n.pipe';

@Component({
  selector: 'app-accounts',
  standalone: true,
  imports: [ReactiveFormsModule, CurrencyPipe, MatButtonModule, MatCardModule, MatCheckboxModule, MatFormFieldModule, MatInputModule, I18nPipe],
  template: `
    <mat-card>
      <mat-card-header><mat-card-title>{{ 'accounts.title' | i18n }}</mat-card-title></mat-card-header>
      <mat-card-content>
        <form [formGroup]="form" (ngSubmit)="create()" style="display:grid;grid-template-columns:2fr 1fr auto;gap:12px;align-items:center;margin-bottom:16px;">
          <mat-form-field><mat-label>{{ 'common.name' | i18n }}</mat-label><input matInput formControlName="name" /></mat-form-field>
          <mat-checkbox formControlName="isDefault">{{ 'accounts.defaultLabel' | i18n }}</mat-checkbox>
          <button mat-flat-button type="submit">{{ 'common.create' | i18n }}</button>
        </form>
        <div style="display:grid;grid-template-columns:repeat(auto-fit,minmax(260px,1fr));gap:16px;">
          @for (account of accounts(); track account.id) {
            <mat-card>
              <mat-card-title>{{ account.name }}</mat-card-title>
              <mat-card-subtitle>{{ account.ownerUsername }}</mat-card-subtitle>
              <mat-card-content>{{ account.balance | currency:'EUR' }}</mat-card-content>
              <mat-card-actions><button mat-button (click)="remove(account.id)">{{ 'common.delete' | i18n }}</button></mat-card-actions>
            </mat-card>
          }
        </div>
      </mat-card-content>
    </mat-card>
  `
})
export class AccountsComponent {
  private readonly fb = inject(FormBuilder);
  private readonly accountService = inject(AccountService);

  readonly accounts = signal<Account[]>([]);
  readonly form = this.fb.nonNullable.group({
    name: ['', Validators.required],
    isDefault: false
  });

  constructor() {
    this.load();
  }

  create(): void {
    if (this.form.invalid) return;
    this.accountService.createAccount(this.form.getRawValue()).subscribe(() => {
      this.form.reset({ name: '', isDefault: false });
      this.load();
    });
  }

  remove(id: number): void {
    this.accountService.deleteAccount(id).subscribe(() => this.load());
  }

  private load(): void {
    this.accountService.getAccounts().subscribe((accounts) => this.accounts.set(accounts));
  }
}
