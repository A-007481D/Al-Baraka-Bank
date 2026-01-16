import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { UserResponse, UserRequest } from '../models/user.model';

@Injectable({
    providedIn: 'root'
})
export class UserService {
    private apiUrl = `${environment.apiUrl}/api/admin/users`;

    constructor(private http: HttpClient) { }

    getAllUsers(): Observable<UserResponse[]> {
        return this.http.get<UserResponse[]>(this.apiUrl);
    }

    getUser(id: number): Observable<UserResponse> {
        return this.http.get<UserResponse>(`${this.apiUrl}/${id}`);
    }

    createUser(user: UserRequest): Observable<UserResponse> {
        return this.http.post<UserResponse>(this.apiUrl, user);
    }

    updateUser(id: number, user: UserRequest): Observable<UserResponse> {
        return this.http.put<UserResponse>(`${this.apiUrl}/${id}`, user);
    }

    deleteUser(id: number): Observable<void> {
        return this.http.delete<void>(`${this.apiUrl}/${id}`);
    }

    activateUser(id: number): Observable<UserResponse> {
        return this.http.put<UserResponse>(`${this.apiUrl}/${id}/activate`, {});
    }

    deactivateUser(id: number): Observable<UserResponse> {
        return this.http.put<UserResponse>(`${this.apiUrl}/${id}/deactivate`, {});
    }
}
