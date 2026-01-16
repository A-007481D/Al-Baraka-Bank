import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Operation, OperationRequest } from '../models/operation.model';

@Injectable({
    providedIn: 'root'
})
export class OperationService {
    private clientUrl = `${environment.apiUrl}/api/client/operations`;
    private agentUrl = `${environment.apiUrl}/api/agent/operations`;

    constructor(private http: HttpClient) { }

    // Client operations
    createOperation(request: OperationRequest): Observable<Operation> {
        return this.http.post<Operation>(this.clientUrl, request);
    }

    getMyOperations(): Observable<Operation[]> {
        return this.http.get<Operation[]>(this.clientUrl);
    }

    uploadDocument(operationId: number, file: File): Observable<string> {
        const formData = new FormData();
        formData.append('file', file);
        return this.http.post(`${this.clientUrl}/${operationId}/document`, formData, {
            responseType: 'text'
        });
    }

    // Agent operations
    getPendingOperations(): Observable<Operation[]> {
        return this.http.get<Operation[]>(`${this.agentUrl}/pending`);
    }

    approveOperation(id: number): Observable<Operation> {
        return this.http.put<Operation>(`${this.agentUrl}/${id}/approve`, {});
    }

    rejectOperation(id: number): Observable<Operation> {
        return this.http.put<Operation>(`${this.agentUrl}/${id}/reject`, {});
    }

    // Pass token for iframe/new tab access where Authorization header can't be set
    getDocumentUrl(operationId: number, token?: string): string {
        let url = `${this.agentUrl}/${operationId}/document`;
        if (token) {
            url += `?token=${encodeURIComponent(token)}`;
        }
        return url;
    }

    getDocumentInfo(operationId: number): Observable<DocumentInfo> {
        return this.http.get<DocumentInfo>(`${this.agentUrl}/${operationId}/document/info`);
    }
}

export interface DocumentInfo {
    id: number;
    fileName: string;
    fileType: string;
    uploadedAt: string;
}
