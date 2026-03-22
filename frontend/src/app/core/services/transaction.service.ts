import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { CreateTransactionRequest, Transaction, TransactionFilter } from '../models/transaction.model';

@Injectable({ providedIn: 'root' })
export class TransactionService {
  private readonly baseUrl = `${environment.apiBaseUrl}/transactions`;

  constructor(private readonly http: HttpClient) {}

  getTransactions(filter?: TransactionFilter): Observable<Transaction[]> {
    let params = new HttpParams();
    if (filter?.startDate) params = params.set('startDate', filter.startDate);
    if (filter?.endDate) params = params.set('endDate', filter.endDate);
    if (filter?.subCategoryId) params = params.set('subCategoryId', String(filter.subCategoryId));
    if (filter?.type) params = params.set('type', filter.type);
    if (filter?.accountId) params = params.set('accountId', String(filter.accountId));
    if (filter?.userId) params = params.set('userId', String(filter.userId));
    return this.http.get<Transaction[]>(this.baseUrl, { params });
  }

  createTransaction(payload: CreateTransactionRequest): Observable<Transaction> {
    return this.http.post<Transaction>(this.baseUrl, payload);
  }

  deleteTransaction(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
