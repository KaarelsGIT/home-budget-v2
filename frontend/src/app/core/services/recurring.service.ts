import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { map, Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { RecurringTransaction, RecurringTransactionRequest } from '../models/recurring.model';

@Injectable({ providedIn: 'root' })
export class RecurringService {
  private readonly baseUrl = `${environment.apiBaseUrl}/recurring`;

  constructor(private readonly http: HttpClient) {}

  getRecurring(): Observable<RecurringTransaction[]> {
    return this.http.get<RecurringTransaction[]>(this.baseUrl);
  }

  getUpcomingRecurring(): Observable<RecurringTransaction[]> {
    return this.getRecurring().pipe(
      map((rows) => rows.filter((row) => row.active).sort((a, b) => a.nextExecutionDate.localeCompare(b.nextExecutionDate)))
    );
  }

  createRecurring(payload: RecurringTransactionRequest): Observable<RecurringTransaction> {
    return this.http.post<RecurringTransaction>(this.baseUrl, payload);
  }

  updateRecurring(id: number, payload: RecurringTransactionRequest): Observable<RecurringTransaction> {
    return this.http.put<RecurringTransaction>(`${this.baseUrl}/${id}`, payload);
  }

  deleteRecurring(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
