// User model interfaces for admin module

export type UserRole = 'CLIENT' | 'AGENT_BANCAIRE' | 'ADMIN';

export interface UserResponse {
    id: number;
    fullName: string;
    email: string;
    role: UserRole;
    active: boolean;
    createdAt: string;
    accountNumber?: string;
}

export interface UserRequest {
    fullName: string;
    email: string;
    password?: string;
    role: UserRole;
    active: boolean;
}
