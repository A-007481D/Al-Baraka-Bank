import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';

export const routes: Routes = [
    // Default
    {
        path: '',
        redirectTo: '/auth/login',
        pathMatch: 'full'
    },

    // public
    {
        path: 'auth',
        children: [
            {
                path: 'login',
                loadComponent: () => import('./features/auth/login/login.component').then(m => m.LoginComponent)
            },
            {
                path: 'register',
                loadComponent: () => import('./features/auth/register/register.component').then(m => m.RegisterComponent)
            }
        ]
    },

    {
        path: 'client',
        canActivate: [authGuard, roleGuard],
        data: { roles: ['CLIENT'] },
        children: [
            {
                path: 'dashboard',
                loadComponent: () => import('./features/client/dashboard/dashboard.component').then(m => m.DashboardComponent)
            }
        ]
    },

    {
        path: 'agent',
        canActivate: [authGuard, roleGuard],
        data: { roles: ['AGENT_BANCAIRE'] },
        children: [
            {
                path: 'dashboard',
                loadComponent: () => import('./features/agent/dashboard/dashboard.component').then(m => m.DashboardComponent)
            }
        ]
    },

    {
        path: 'admin',
        canActivate: [authGuard, roleGuard],
        data: { roles: ['ADMIN'] },
        children: [
            {
                path: 'dashboard',
                loadComponent: () => import('./features/admin/dashboard/dashboard.component').then(m => m.DashboardComponent)
            }
        ]
    },

    {
        path: 'unauthorized',
        loadComponent: () => import('./shared/unauthorized/unauthorized.component').then(m => m.UnauthorizedComponent)
    },

    // Fallback
    {
        path: '**',
        redirectTo: '/auth/login'
    }
];
