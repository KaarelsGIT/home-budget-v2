import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { RecurringPayment, RecurringPaymentNotification, RecurringPaymentRequest } from '../models/recurring.model';

@Injectable({ providedIn: 'root' })
export class RecurringService {
  private readonly baseUrl = `${environment.apiBaseUrl}/recurring-payments`;

  constructor(private readonly http: HttpClient) {}

  getRecurringPayments(): Observable<RecurringPayment[]> {
    return this.http.get<RecurringPayment[]>(this.baseUrl);
  }

  getNotifications(): Observable<RecurringPaymentNotification[]> {
    return this.http.get<RecurringPaymentNotification[]>(`${this.baseUrl}/notifications`);
  }

  createRecurringPayment(payload: RecurringPaymentRequest): Observable<RecurringPayment> {
    return this.http.post<RecurringPayment>(this.baseUrl, payload);
  }

  markPaid(id: number, transactionId: number): Observable<RecurringPayment> {
    return this.http.post<RecurringPayment>(`${this.baseUrl}/${id}/mark-paid`, { transactionId });
  }
}
