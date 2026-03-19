import { Component, computed, inject, signal } from '@angular/core';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatTableModule } from '@angular/material/table';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { ReportService } from '../../core/services/report.service';
import { FamilyOverviewResponse, OverviewMonthlyItem } from '../../core/models/report.model';

@Component({
  selector: 'app-overview',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    CurrencyPipe,
    DatePipe,
    MatCardModule,
    MatFormFieldModule,
    MatSelectModule,
    MatButtonModule,
    MatTableModule,
    MatProgressSpinnerModule
  ],
  styles: `
    .header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      gap: 16px;
      margin-bottom: 16px;
    }

    .filters {
      display: flex;
      gap: 12px;
      align-items: center;
      flex-wrap: wrap;
    }

    .kpis {
      display: grid;
      grid-template-columns: repeat(4, minmax(160px, 1fr));
      gap: 12px;
      margin-bottom: 16px;
    }

    .kpi {
      border-radius: 14px;
      padding: 16px;
      background: linear-gradient(135deg, #f4f8ff, #ffffff);
      border: 1px solid #d9e7fb;
    }

    .kpi .label {
      font-size: 0.8rem;
      color: #5b6572;
      text-transform: uppercase;
      letter-spacing: 0.08em;
    }

    .kpi .value {
      font-size: 1.3rem;
      font-weight: 700;
      margin-top: 6px;
    }

    .chart-card,
    .table-card {
      margin-bottom: 16px;
    }

    .bars {
      display: grid;
      gap: 10px;
    }

    .bar-row {
      display: grid;
      grid-template-columns: 52px 1fr auto;
      gap: 10px;
      align-items: center;
    }

    .track {
      height: 14px;
      border-radius: 10px;
      background: #edf2f8;
      overflow: hidden;
      position: relative;
    }

    .income {
      height: 100%;
      background: #26a269;
    }

    .expense {
      height: 100%;
      background: #cf3a5b;
      position: absolute;
      top: 0;
      left: 0;
      opacity: 0.65;
    }

    .muted {
      color: #657487;
      font-size: 0.85rem;
    }

    @media (max-width: 900px) {
      .kpis {
        grid-template-columns: repeat(2, minmax(150px, 1fr));
      }
    }
  `,
  template: `
    <div class="header">
      <h2>Pere ülevaade</h2>
      <form [formGroup]="filters" class="filters">
        <mat-form-field>
          <mat-label>Aasta</mat-label>
          <mat-select formControlName="year">
            @for (year of years; track year) {
              <mat-option [value]="year">{{ year }}</mat-option>
            }
          </mat-select>
        </mat-form-field>

        <mat-form-field>
          <mat-label>Kuu</mat-label>
          <mat-select formControlName="month">
            <mat-option [value]="null">Kõik kuud</mat-option>
            @for (month of months; track month.value) {
              <mat-option [value]="month.value">{{ month.label }}</mat-option>
            }
          </mat-select>
        </mat-form-field>

        <button mat-flat-button color="primary" type="button" (click)="reload()">Rakenda</button>
      </form>
    </div>

    @if (loading()) {
      <mat-spinner diameter="44" />
    } @else {
      <section class="kpis">
        <div class="kpi">
          <div class="label">Tulud</div>
          <div class="value">{{ report()?.totals?.income | currency: 'EUR' }}</div>
        </div>
        <div class="kpi">
          <div class="label">Kulud</div>
          <div class="value">{{ report()?.totals?.expense | currency: 'EUR' }}</div>
        </div>
        <div class="kpi">
          <div class="label">Ülekanded</div>
          <div class="value">{{ report()?.totals?.transferOut | currency: 'EUR' }}</div>
        </div>
        <div class="kpi">
          <div class="label">Neto</div>
          <div class="value">{{ report()?.totals?.net | currency: 'EUR' }}</div>
        </div>
      </section>

      <mat-card class="chart-card">
        <mat-card-header>
          <mat-card-title>Jooksev aasta: kuu lõikes trend</mat-card-title>
        </mat-card-header>
        <mat-card-content>
          <div class="bars">
            @for (item of report()?.monthly ?? []; track item.month) {
              <div class="bar-row">
                <div class="muted">{{ monthName(item.month) }}</div>
                <div class="track">
                  <div class="income" [style.width.%]="incomeWidth(item)"></div>
                  <div class="expense" [style.width.%]="expenseWidth(item)"></div>
                </div>
                <div class="muted">
                  +{{ item.income | currency: 'EUR' }} / -{{ item.expense | currency: 'EUR' }}
                </div>
              </div>
            }
          </div>
        </mat-card-content>
      </mat-card>

      <mat-card class="table-card">
        <mat-card-header>
          <mat-card-title>Kategooriate lõikes (professionaalne vaade)</mat-card-title>
        </mat-card-header>
        <mat-card-content>
          <table mat-table [dataSource]="report()?.categories ?? []" class="full-width">
            <ng-container matColumnDef="parent">
              <th mat-header-cell *matHeaderCellDef>Parent</th>
              <td mat-cell *matCellDef="let row">{{ row.parentCategory }}</td>
            </ng-container>
            <ng-container matColumnDef="sub">
              <th mat-header-cell *matHeaderCellDef>Subcategory</th>
              <td mat-cell *matCellDef="let row">{{ row.subCategory }}</td>
            </ng-container>
            <ng-container matColumnDef="income">
              <th mat-header-cell *matHeaderCellDef>Tulud</th>
              <td mat-cell *matCellDef="let row">{{ row.income | currency: 'EUR' }}</td>
            </ng-container>
            <ng-container matColumnDef="expense">
              <th mat-header-cell *matHeaderCellDef>Kulud</th>
              <td mat-cell *matCellDef="let row">{{ row.expense | currency: 'EUR' }}</td>
            </ng-container>

            <tr mat-header-row *matHeaderRowDef="categoryColumns"></tr>
            <tr mat-row *matRowDef="let row; columns: categoryColumns"></tr>
          </table>
        </mat-card-content>
      </mat-card>

      <mat-card>
        <mat-card-header>
          <mat-card-title>Pere tehingud</mat-card-title>
        </mat-card-header>
        <mat-card-content>
          <table mat-table [dataSource]="report()?.transactions ?? []" class="full-width">
            <ng-container matColumnDef="date">
              <th mat-header-cell *matHeaderCellDef>Kuupäev</th>
              <td mat-cell *matCellDef="let row">{{ row.date | date }}</td>
            </ng-container>
            <ng-container matColumnDef="type">
              <th mat-header-cell *matHeaderCellDef>Tüüp</th>
              <td mat-cell *matCellDef="let row">{{ row.type }}</td>
            </ng-container>
            <ng-container matColumnDef="category">
              <th mat-header-cell *matHeaderCellDef>Kategooria</th>
              <td mat-cell *matCellDef="let row">{{ formatCategory(row) }}</td>
            </ng-container>
            <ng-container matColumnDef="amount">
              <th mat-header-cell *matHeaderCellDef>Summa</th>
              <td mat-cell *matCellDef="let row">{{ row.amount | currency: 'EUR' }}</td>
            </ng-container>

            <tr mat-header-row *matHeaderRowDef="transactionColumns"></tr>
            <tr mat-row *matRowDef="let row; columns: transactionColumns"></tr>
          </table>
        </mat-card-content>
      </mat-card>
    }
  `
})
export class OverviewComponent {
  private readonly fb = inject(FormBuilder);
  private readonly reportService = inject(ReportService);

  readonly loading = signal(true);
  readonly report = signal<FamilyOverviewResponse | null>(null);

  readonly categoryColumns = ['parent', 'sub', 'income', 'expense'];
  readonly transactionColumns = ['date', 'type', 'category', 'amount'];

  readonly years = Array.from({ length: 6 }, (_, i) => new Date().getFullYear() - i);
  readonly months = [
    { value: 1, label: 'Jaanuar' },
    { value: 2, label: 'Veebruar' },
    { value: 3, label: 'Märts' },
    { value: 4, label: 'Aprill' },
    { value: 5, label: 'Mai' },
    { value: 6, label: 'Juuni' },
    { value: 7, label: 'Juuli' },
    { value: 8, label: 'August' },
    { value: 9, label: 'September' },
    { value: 10, label: 'Oktoober' },
    { value: 11, label: 'November' },
    { value: 12, label: 'Detsember' }
  ];

  readonly filters = this.fb.group({
    year: [new Date().getFullYear()],
    month: [null as number | null]
  });

  readonly maxMonthlyAmount = computed(() => {
    const monthly = this.report()?.monthly ?? [];
    if (!monthly.length) {
      return 1;
    }
    const max = monthly.reduce((acc, item) => Math.max(acc, Number(item.income), Number(item.expense)), 0);
    return max === 0 ? 1 : max;
  });

  constructor() {
    this.reload();
  }

  reload(): void {
    this.loading.set(true);
    const value = this.filters.getRawValue();

    this.reportService.getFamilyOverview(value.year ?? new Date().getFullYear(), value.month ?? null).subscribe({
      next: (report) => {
        this.report.set(report);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
      }
    });
  }

  incomeWidth(item: OverviewMonthlyItem): number {
    return (Number(item.income) / this.maxMonthlyAmount()) * 100;
  }

  expenseWidth(item: OverviewMonthlyItem): number {
    return (Number(item.expense) / this.maxMonthlyAmount()) * 100;
  }

  monthName(month: number): string {
    return this.months.find((m) => m.value === month)?.label ?? String(month);
  }

  formatCategory(transaction: { parentCategoryName?: string | null; subCategoryName?: string | null; categoryName?: string | null }): string {
    if (transaction.parentCategoryName && transaction.subCategoryName) {
      return `${transaction.parentCategoryName} / ${transaction.subCategoryName}`;
    }

    return transaction.categoryName ?? '-';
  }
}
