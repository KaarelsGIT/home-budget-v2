import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { FamilyOverviewResponse } from '../models/report.model';

@Injectable({ providedIn: 'root' })
export class ReportService {
  private readonly baseUrl = `${environment.apiBaseUrl}/reports`;

  constructor(private readonly http: HttpClient) {}

  getFamilyOverview(year: number, month?: number | null): Observable<FamilyOverviewResponse> {
    let params = new HttpParams().set('year', String(year));
    if (month) {
      params = params.set('month', String(month));
    }

    return this.http.get<FamilyOverviewResponse>(`${this.baseUrl}/family-overview`, { params });
  }
}
