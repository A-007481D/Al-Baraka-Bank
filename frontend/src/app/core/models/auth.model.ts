export interface User {
    id?: number;
    fullName: string;
    email: string;
    role: 'CLIENT' | 'AGENT_BANCAIRE' | 'ADMIN';
    active?: boolean;
}

export interface LoginRequest {
    email: string;
    password: string;
}

export interface RegisterRequest {
    fullName: string;
    email: string;
    password: string;
}

export interface AuthResponse {
    token: string;
}

export interface JwtPayload {
    sub: string;  // email
    role: string;
    exp: number;
}
