import { AfterViewInit, Component, ElementRef, ViewChild, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { Chart, PieController, ArcElement, CategoryScale, LinearScale, LineController, LineElement, PointElement, BarController, BarElement, Tooltip, Legend } from 'chart.js';
import { forkJoin } from 'rxjs';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { CurrencyPipe } from '@angular/common';
import { I18nPipe } from '../../shared/pipes/i18n.pipe';
import { StatsCategoryItem, StatsMonthlyItem, StatsSummary, StatsTrendItem } from '../../core/models/stats.model';
import { UserSummary } from '../../core/models/user.model';
import { AuthService } from '../../core/services/auth.service';
import { StatsService } from '../../core/services/stats.service';
import { UserService } from '../../core/services/user.service';

Chart.register(PieController, ArcElement, CategoryScale, LinearScale, LineController, LineElement, PointElement, BarController, BarElement, Tooltip, Legend);

@Component({
  selector: 'app-stats',
  standalone: true,
  imports: [ReactiveFormsModule, CurrencyPipe, MatButtonModule, MatCardModule, MatFormFieldModule, MatSelectModule, I18nPipe],
  template: `
    <mat-card>
      <mat-card-header><mat-card-title>{{ 'stats.title' | i18n }}</mat-card-title></mat-card-header>
      <mat-card-content>
        <form [formGroup]="form" style="display:grid;grid-template-columns:repeat(3,minmax(0,1fr));gap:12px;margin-bottom:16px;">
          <mat-form-field><mat-label>{{ 'common.year' | i18n }}</mat-label><mat-select formControlName="year">@for (year of years; track year) { <mat-option [value]="year">{{ year }}</mat-option> }</mat-select></mat-form-field>
          <mat-form-field><mat-label>{{ 'common.month' | i18n }}</mat-label><mat-select formControlName="month"><mat-option [value]="null">{{ 'common.all' | i18n }}</mat-option>@for (month of months; track month.value) { <mat-option [value]="month.value">{{ month.label }}</mat-option> }</mat-select></mat-form-field>
          @if (showUserFilter()) {
            <mat-form-field><mat-label>{{ 'stats.userFilter' | i18n }}</mat-label><mat-select formControlName="userId"><mat-option [value]="null">{{ 'common.all' | i18n }}</mat-option>@for (user of users(); track user.id) { <mat-option [value]="user.id">{{ user.username }}</mat-option> }</mat-select></mat-form-field>
          }
          <button mat-flat-button type="button" (click)="load()">{{ 'stats.summary' | i18n }}</button>
        </form>

        <div style="display:grid;grid-template-columns:repeat(3,minmax(0,1fr));gap:12px;margin-bottom:16px;">
          <mat-card><mat-card-title>{{ 'transactions.income' | i18n }}</mat-card-title><mat-card-content>{{ summary()?.income | currency:'EUR' }}</mat-card-content></mat-card>
          <mat-card><mat-card-title>{{ 'transactions.expense' | i18n }}</mat-card-title><mat-card-content>{{ summary()?.expense | currency:'EUR' }}</mat-card-content></mat-card>
          <mat-card><mat-card-title>{{ 'stats.summary' | i18n }}</mat-card-title><mat-card-content>{{ summary()?.net | currency:'EUR' }}</mat-card-content></mat-card>
        </div>

        <div style="display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:16px;">
          <mat-card><mat-card-title>{{ 'stats.trends' | i18n }}</mat-card-title><mat-card-content><canvas #trendCanvas></canvas></mat-card-content></mat-card>
          <mat-card><mat-card-title>{{ 'stats.category' | i18n }}</mat-card-title><mat-card-content><canvas #pieCanvas></canvas></mat-card-content></mat-card>
          <mat-card style="grid-column:span 2;"><mat-card-title>{{ 'stats.monthly' | i18n }}</mat-card-title><mat-card-content><canvas #barCanvas></canvas></mat-card-content></mat-card>
        </div>
      </mat-card-content>
    </mat-card>
  `
})
export class StatsComponent implements AfterViewInit {
  private readonly fb = inject(FormBuilder);
  private readonly statsService = inject(StatsService);
  private readonly userService = inject(UserService);
  private readonly authService = inject(AuthService);

  @ViewChild('trendCanvas') trendCanvas?: ElementRef<HTMLCanvasElement>;
  @ViewChild('pieCanvas') pieCanvas?: ElementRef<HTMLCanvasElement>;
  @ViewChild('barCanvas') barCanvas?: ElementRef<HTMLCanvasElement>;

  readonly years = [2024, 2025, 2026];
  readonly months = [
    { value: 1, label: 'Jan' }, { value: 2, label: 'Feb' }, { value: 3, label: 'Mar' }, { value: 4, label: 'Apr' },
    { value: 5, label: 'May' }, { value: 6, label: 'Jun' }, { value: 7, label: 'Jul' }, { value: 8, label: 'Aug' },
    { value: 9, label: 'Sep' }, { value: 10, label: 'Oct' }, { value: 11, label: 'Nov' }, { value: 12, label: 'Dec' }
  ];
  readonly users = signal<UserSummary[]>([]);
  readonly summary = signal<StatsSummary | null>(null);
  readonly monthly = signal<StatsMonthlyItem[]>([]);
  readonly categories = signal<StatsCategoryItem[]>([]);
  readonly trends = signal<StatsTrendItem[]>([]);

  readonly form = this.fb.group({
    year: [2026],
    month: [null as number | null],
    userId: [null as number | null]
  });

  private trendChart?: Chart;
  private pieChart?: Chart;
  private barChart?: Chart;

  constructor() {
    this.userService.getUsers().subscribe((users) => this.users.set(users));
  }

  ngAfterViewInit(): void {
    this.load();
  }

  showUserFilter(): boolean {
    const role = this.authService.currentUser()?.role;
    return role === 'PARENT' || role === 'ADMIN';
  }

  load(): void {
    const value = this.form.getRawValue();
    const year = value.year ?? 2026;
    forkJoin({
      summary: this.statsService.getSummary(year, value.month, value.userId),
      monthly: this.statsService.getMonthly(year, value.userId),
      categories: this.statsService.getCategory(year, value.month, value.userId),
      trends: this.statsService.getTrends(year, value.userId)
    }).subscribe(({ summary, monthly, categories, trends }) => {
      this.summary.set(summary);
      this.monthly.set(monthly);
      this.categories.set(categories);
      this.trends.set(trends);
      this.renderCharts();
    });
  }

  private renderCharts(): void {
    this.trendChart?.destroy();
    this.pieChart?.destroy();
    this.barChart?.destroy();
    if (!this.trendCanvas || !this.pieCanvas || !this.barCanvas) return;

    this.trendChart = new Chart(this.trendCanvas.nativeElement, {
      type: 'line',
      data: {
        labels: this.trends().map((item) => item.label),
        datasets: [
          { label: 'Income', data: this.trends().map((item) => item.income), borderColor: '#16a34a' },
          { label: 'Expense', data: this.trends().map((item) => item.expense), borderColor: '#dc2626' }
        ]
      }
    });

    this.pieChart = new Chart(this.pieCanvas.nativeElement, {
      type: 'pie',
      data: {
        labels: this.categories().map((item) => `${item.categoryName} / ${item.subCategoryName}`),
        datasets: [{ data: this.categories().map((item) => item.amount), backgroundColor: ['#2563eb', '#f97316', '#16a34a', '#dc2626', '#7c3aed'] }]
      }
    });

    this.barChart = new Chart(this.barCanvas.nativeElement, {
      type: 'bar',
      data: {
        labels: this.monthly().map((item) => String(item.month)),
        datasets: [
          { label: 'Income', data: this.monthly().map((item) => item.income), backgroundColor: '#22c55e' },
          { label: 'Expense', data: this.monthly().map((item) => item.expense), backgroundColor: '#ef4444' }
        ]
      }
    });
  }
}
