import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Account } from '../models/operation.model';

@Injectable({
    providedIn: 'root'
})
export class AccountService {
    private apiUrl = `${environment.apiUrl}/api/client`;

    constructor(private http: HttpClient) { }

    getMyAccount(): Observable<Account> {
        return this.http.get<Account>(`${this.apiUrl}/account`);
    }
}
