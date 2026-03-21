import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  Transaction,
  TransactionFilter,
  TransactionRequest,
  TransferRequestApi
} from '../models/transaction.model';

@Injectable({ providedIn: 'root' })
export class TransactionService {
  private readonly baseUrl = `${environment.apiBaseUrl}/transactions`;
  private readonly transferUrl = `${environment.apiBaseUrl}/transfers`;

  constructor(private readonly http: HttpClient) {}

  getTransactions(): Observable<Transaction[]> {
    return this.http.get<Transaction[]>(this.baseUrl);
  }

  getTransactionById(id: number): Observable<Transaction> {
    return this.http.get<Transaction>(`${this.baseUrl}/${id}`);
  }

  createTransaction(payload: TransactionRequest): Observable<Transaction> {
    return this.http.post<Transaction>(this.baseUrl, payload);
  }

  updateTransaction(id: number, payload: TransactionRequest): Observable<Transaction> {
    return this.http.put<Transaction>(`${this.baseUrl}/${id}`, payload);
  }

  transfer(payload: TransferRequestApi): Observable<Transaction> {
    return this.http.post<Transaction>(this.transferUrl, payload);
  }

  deleteTransaction(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  filterTransactions(filter: TransactionFilter): Observable<Transaction[]> {
    let params = new HttpParams();

    if (filter.startDate) {
      params = params.set('startDate', filter.startDate);
    }
    if (filter.endDate) {
      params = params.set('endDate', filter.endDate);
    }
    if (filter.categoryId) {
      params = params.set('categoryId', String(filter.categoryId));
    }
    if (filter.type) {
      params = params.set('type', filter.type);
    }
    if (filter.accountId) {
      params = params.set('accountId', String(filter.accountId));
    }

    params = params.set('sortBy', filter.sortBy ?? 'createdAt').set('direction', filter.direction ?? 'DESC');

    return this.http.get<Transaction[]>(`${this.baseUrl}/filter`, { params });
  }
}
