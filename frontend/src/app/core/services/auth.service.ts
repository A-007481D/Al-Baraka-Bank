import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { Router } from '@angular/router';
import { StorageService } from './storage.service';
import { AuthResponse, LoginRequest, RegisterRequest, JwtPayload, User } from '../models/auth.model';
import { environment } from '../../../environments/environment';

@Injectable({
    providedIn: 'root'
})
export class AuthService {
    private readonly TOKEN_KEY = 'jwt_token';
    private readonly USER_KEY = 'current_user';

    private currentUserSubject = new BehaviorSubject<User | null>(this.getCurrentUser());
    public currentUser$ = this.currentUserSubject.asObservable();

    constructor(
        private http: HttpClient,
        private storage: StorageService,
        private router: Router
    ) { }


    login(credentials: LoginRequest): Observable<AuthResponse> {
        return this.http.post<AuthResponse>(`${environment.apiUrl}/auth/login`, credentials)
            .pipe(
                tap(response => {
                    this.setToken(response.token);
                    const user = this.decodeToken(response.token);
                    this.setCurrentUser(user);
                    this.currentUserSubject.next(user);
                })
            );
    }

    /**
     * new CLIENT
     */
    register(userData: RegisterRequest): Observable<AuthResponse> {
        return this.http.post<AuthResponse>(`${environment.apiUrl}/auth/register`, userData)
            .pipe(
                tap(response => {
                    this.setToken(response.token);
                    const user = this.decodeToken(response.token);
                    this.setCurrentUser(user);
                    this.currentUserSubject.next(user);
                })
            );
    }


    logout(): void {
        this.storage.removeItem(this.TOKEN_KEY);
        this.storage.removeItem(this.USER_KEY);
        this.currentUserSubject.next(null);
        this.router.navigate(['/auth/login']);
    }


    getToken(): string | null {
        return this.storage.getItem(this.TOKEN_KEY);
    }


    isAuthenticated(): boolean {
        const token = this.getToken();
        if (!token) return false;
        return !this.isTokenExpired(token);
    }

  
    getUserRole(): string | null {
        const user = this.getCurrentUser();
        return user?.role || null;
    }


    getCurrentUser(): User | null {
        const userJson = this.storage.getItem(this.USER_KEY);
        return userJson ? JSON.parse(userJson) : null;
    }

  
    private decodeToken(token: string): User {
        const payload = this.parseJwt(token);
        return {
            email: payload.sub,
            fullName: payload.sub.split('@')[0],
            role: payload.role as 'CLIENT' | 'AGENT_BANCAIRE' | 'ADMIN'
        };
    }

    private parseJwt(token: string): JwtPayload {
        const base64Url = token.split('.')[1];
        const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
        const jsonPayload = decodeURIComponent(
            atob(base64)
                .split('')
                .map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
                .join('')
        );
        return JSON.parse(jsonPayload);
    }

    
    private isTokenExpired(token: string): boolean {
        try {
            const payload = this.parseJwt(token);
            const expiryTime = payload.exp * 1000; // convert to milliseconds
            return Date.now() >= expiryTime;
        } catch {
            return true;
        }
    }

    
    private setToken(token: string): void {
        this.storage.setItem(this.TOKEN_KEY, token);
    }

    private setCurrentUser(user: User): void {
        this.storage.setItem(this.USER_KEY, JSON.stringify(user));
    }
}
