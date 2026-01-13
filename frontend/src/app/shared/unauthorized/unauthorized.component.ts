import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

@Component({
  selector: 'app-unauthorized',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="min-h-screen bg-gradient-to-br from-red-500 to-red-700 flex items-center justify-center p-6">
      <div class="bg-white rounded-2xl shadow-2xl max-w-md w-full p-8 text-center">
        <!-- Icon -->
        <div class="text-6xl mb-4">🚫</div>
        
        <!-- Title -->
        <h1 class="text-3xl font-bold text-red-600 mb-4">Access Denied</h1>
        
        <!-- Message -->
        <p class="text-gray-700 mb-6">
          You don't have permission to access this page.
          Please contact your administrator if you believe this is an error.
        </p>
        
        <!-- Action Buttons -->
        <div class="space-y-3">
          <button
            (click)="goBack()"
            class="w-full bg-primary hover:bg-blue-700 text-white py-3 px-4 rounded-lg transition"
          >
            Go Back
          </button>
          
          <button
            (click)="navigateToLogin()"
            class="w-full bg-gray-200 hover:bg-gray-300 text-gray-800 py-3 px-4 rounded-lg transition"
          >
            Back to Login
          </button>
        </div>
      </div>
    </div>
  `,
  styles: []
})
export class UnauthorizedComponent {
  constructor(private router: Router) { }

  goBack() {
    window.history.back();
  }

  navigateToLogin() {
    this.router.navigate(['/auth/login']);
  }
}
