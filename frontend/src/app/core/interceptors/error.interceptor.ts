import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { Router } from '@angular/router';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
    const router = inject(Router);

    return next(req).pipe(
        catchError((error: HttpErrorResponse) => {
            let errorMessage = 'An error occurred';

            if (error.error instanceof ErrorEvent) {
                // Client-side error
                errorMessage = `Error: ${error.error.message}`;
            } else {
                switch (error.status) {
                    case 401:
                        errorMessage = 'Unauthorized - Please login';
                        router.navigate(['/auth/login']);
                        break;
                    case 403:
                        errorMessage = 'Access Denied - Insufficient permissions';
                        break;
                    case 404:
                        errorMessage = 'Resource not found';
                        break;
                    case 500:
                        errorMessage = 'Server error - Please try again later';
                        break;
                    default:
                        errorMessage = error.error?.message || `Error: ${error.status}`;
                }
            }

            console.error('HTTP Error:', errorMessage, error);
            return throwError(() => new Error(errorMessage));
        })
    );
};
