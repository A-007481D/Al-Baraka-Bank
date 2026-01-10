import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
    selector: 'app-login',
    standalone: true,
    imports: [CommonModule, ReactiveFormsModule],
    templateUrl: './login.component.html',
    styleUrl: './login.component.css'
})
export class LoginComponent {
    loginForm: FormGroup;
    errorMessage = '';
    loading = false;

    constructor(
        private fb: FormBuilder,
        private authService: AuthService,
        private router: Router
    ) {
        this.loginForm = this.fb.group({
            email: ['', [Validators.required, Validators.email]],
            password: ['', [Validators.required, Validators.minLength(6)]]
        });
    }

    get email() {
        return this.loginForm.get('email');
    }

    get password() {
        return this.loginForm.get('password');
    }

    onSubmit(): void {
        if (this.loginForm.invalid) {
            this.loginForm.markAllAsTouched();
            return;
        }

        this.loading = true;
        this.errorMessage = '';

        console.log('Attempting login with:', this.loginForm.value.email);

        this.authService.login(this.loginForm.value).subscribe({
            next: (response) => {
                console.log('Login successful, token received');
                this.loading = false;

                const role = this.authService.getUserRole();
                console.log('User role:', role);

                switch (role) {
                    case 'CLIENT':
                        console.log('Navigating to client dashboard');
                        this.router.navigate(['/client/dashboard']);
                        break;
                    case 'AGENT_BANCAIRE':
                        console.log('Navigating to agent dashboard');
                        this.router.navigate(['/agent/dashboard']);
                        break;
                    case 'ADMIN':
                        console.log('Navigating to admin dashboard');
                        this.router.navigate(['/admin/dashboard']);
                        break;
                    default:
                        console.log('Unknown role, navigating to home');
                        this.router.navigate(['/']);
                }
            },
            error: (error) => {
                console.error('Login error:', error);
                this.errorMessage = error.error?.message || error.message || 'Login failed. Please check your credentials.';
                this.loading = false;
            }
        });
    }

    navigateToRegister(): void {
        this.router.navigate(['/auth/register']);
    }
}
