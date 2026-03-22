import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Account, AccountRequest } from '../models/account.model';

@Injectable({ providedIn: 'root' })
export class AccountService {
  private readonly baseUrl = `${environment.apiBaseUrl}/accounts`;

  constructor(private readonly http: HttpClient) {}

  getAccounts(ownerId?: number): Observable<Account[]> {
    const params = ownerId == null ? undefined : new HttpParams().set('ownerId', String(ownerId));
    return this.http.get<Account[]>(this.baseUrl, { params });
  }

  createAccount(payload: AccountRequest): Observable<Account> {
    return this.http.post<Account>(this.baseUrl, payload);
  }

  deleteAccount(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
