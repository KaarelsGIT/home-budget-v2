import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Account, AccountRequest } from '../models/account.model';

@Injectable({ providedIn: 'root' })
export class AccountService {
  private readonly baseUrl = `${environment.apiBaseUrl}/accounts`;

  constructor(private readonly http: HttpClient) {}

  getAccounts(userId?: number): Observable<Account[]> {
    const params = userId == null ? undefined : new HttpParams().set('userId', String(userId));
    return this.http.get<Account[]>(this.baseUrl, { params });
  }

  getAccountById(id: number): Observable<Account> {
    return this.http.get<Account>(`${this.baseUrl}/${id}`);
  }

  createAccount(payload: AccountRequest): Observable<Account> {
    return this.http.post<Account>(this.baseUrl, payload);
  }

  updateAccount(id: number, payload: AccountRequest): Observable<Account> {
    return this.http.put<Account>(`${this.baseUrl}/${id}`, payload);
  }

  deleteAccount(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
