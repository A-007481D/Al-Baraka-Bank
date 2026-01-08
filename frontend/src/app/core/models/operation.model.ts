export type OperationType = 'DEPOSIT' | 'WITHDRAWAL' | 'TRANSFER';

export type OperationStatus = 'PENDING' | 'EXECUTED' | 'APPROVED' | 'REJECTED' | 'CANCELLED';

export interface Operation {
    id: number;
    type: OperationType;
    amount: number;
    status: OperationStatus;
    createdAt: string;
    validatedAt?: string;
    executedAt?: string;
    sourceAccountNumber: string;
    destinationAccountNumber?: string;
    hasDocument: boolean;
    aiAnalysis?: string;
}

export interface OperationRequest {
    type: OperationType;
    amount: number;
    destinationAccountNumber?: string;
}

export interface Account {
    accountNumber: string;
    balance: number;
    ownerName: string;
}
