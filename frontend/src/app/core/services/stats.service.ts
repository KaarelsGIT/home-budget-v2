import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { StatsCategoryItem, StatsMonthlyItem, StatsSummary, StatsTrendItem } from '../models/stats.model';

@Injectable({ providedIn: 'root' })
export class StatsService {
  private readonly baseUrl = `${environment.apiBaseUrl}/stats`;

  constructor(private readonly http: HttpClient) {}

  getSummary(year?: number, month?: number | null, userId?: number | null): Observable<StatsSummary> {
    return this.http.get<StatsSummary>(`${this.baseUrl}/summary`, { params: this.params(year, month, userId) });
  }

  getMonthly(year: number, userId?: number | null): Observable<StatsMonthlyItem[]> {
    return this.http.get<StatsMonthlyItem[]>(`${this.baseUrl}/monthly`, { params: this.params(year, null, userId) });
  }

  getCategory(year?: number, month?: number | null, userId?: number | null): Observable<StatsCategoryItem[]> {
    return this.http.get<StatsCategoryItem[]>(`${this.baseUrl}/category`, { params: this.params(year, month, userId) });
  }

  getTrends(year: number, userId?: number | null): Observable<StatsTrendItem[]> {
    return this.http.get<StatsTrendItem[]>(`${this.baseUrl}/trends`, { params: this.params(year, null, userId) });
  }

  private params(year?: number, month?: number | null, userId?: number | null): HttpParams {
    let params = new HttpParams();
    if (year != null) params = params.set('year', String(year));
    if (month != null) params = params.set('month', String(month));
    if (userId != null) params = params.set('userId', String(userId));
    return params;
  }
}
