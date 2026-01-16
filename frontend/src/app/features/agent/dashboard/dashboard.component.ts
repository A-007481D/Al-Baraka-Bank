import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { AuthService } from '../../../core/services/auth.service';
import { OperationService, DocumentInfo } from '../../../core/services/operation.service';
import { Operation } from '../../../core/models/operation.model';

@Component({
  selector: 'app-agent-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent implements OnInit {
  user: any;
  pendingOperations: Operation[] = [];
  loading = false;
  error = '';
  success = '';

  // Confirmation modal
  showConfirmModal = false;
  confirmAction: 'approve' | 'reject' = 'approve';
  operationToProcess: Operation | null = null;

  // Document viewer modal
  showDocumentModal = false;
  documentInfo: DocumentInfo | null = null;
  documentUrl: string = '';
  safeDocumentUrl: SafeResourceUrl | null = null;
  documentLoading = false;

  // Review modal
  showReviewModal = false;
  operationToReview: Operation | null = null;

  constructor(
    private authService: AuthService,
    private operationService: OperationService,
    private router: Router,
    private sanitizer: DomSanitizer
  ) { }

  ngOnInit() {
    this.user = this.authService.getCurrentUser();
    this.loadPendingOperations();
  }

  loadPendingOperations() {
    this.loading = true;
    this.operationService.getPendingOperations().subscribe({
      next: (operations) => {
        this.pendingOperations = operations;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to load pending operations';
        this.loading = false;
      }
    });
  }

  // Approval actions
  confirmApprove(operation: Operation) {
    this.operationToProcess = operation;
    this.confirmAction = 'approve';
    this.showConfirmModal = true;
  }

  confirmReject(operation: Operation) {
    this.operationToProcess = operation;
    this.confirmAction = 'reject';
    this.showConfirmModal = true;
  }

  cancelAction() {
    this.showConfirmModal = false;
    this.operationToProcess = null;
  }

  processAction() {
    if (!this.operationToProcess) return;

    const action = this.confirmAction === 'approve'
      ? this.operationService.approveOperation(this.operationToProcess.id)
      : this.operationService.rejectOperation(this.operationToProcess.id);

    this.loading = true;
    action.subscribe({
      next: () => {
        this.success = `Operation ${this.confirmAction === 'approve' ? 'approved' : 'rejected'} successfully`;
        this.showConfirmModal = false;
        this.operationToProcess = null;
        this.loadPendingOperations();
        this.clearMessages();
      },
      error: (err) => {
        this.error = err.error?.message || `Failed to ${this.confirmAction} operation`;
        this.showConfirmModal = false;
        this.loading = false;
      }
    });
  }

  // Document viewing
  viewDocument(operation: Operation) {
    if (!operation.hasDocument) return;

    this.documentLoading = true;
    this.operationService.getDocumentInfo(operation.id).subscribe({
      next: (info) => {
        this.documentInfo = info;
        // Get token for iframe/new tab access
        const token = this.authService.getToken();
        this.documentUrl = this.operationService.getDocumentUrl(operation.id, token || undefined);
        this.safeDocumentUrl = this.sanitizer.bypassSecurityTrustResourceUrl(this.documentUrl);
        this.showDocumentModal = true;
        this.documentLoading = false;
      },
      error: (err) => {
        this.error = 'Failed to load document info';
        this.documentLoading = false;
      }
    });
  }

  closeDocumentModal() {
    this.showDocumentModal = false;
    this.documentInfo = null;
    this.documentUrl = '';
    this.safeDocumentUrl = null;
  }

  openDocumentInNewTab() {
    if (this.documentUrl) {
      window.open(this.documentUrl, '_blank');
    }
  }

  // Review modal methods
  openReviewModal(operation: Operation) {
    this.operationToReview = operation;
    this.showReviewModal = true;
  }

  closeReviewModal() {
    this.showReviewModal = false;
    this.operationToReview = null;
  }

  getHighValueCount(): number {
    return this.pendingOperations.filter(op => op.amount > 10000).length;
  }

  clearMessages() {
    setTimeout(() => {
      this.success = '';
      this.error = '';
    }, 3000);
  }

  getTypeIcon(type: string): string {
    switch (type) {
      case 'DEPOSIT': return '↓';
      case 'WITHDRAWAL': return '↑';
      case 'TRANSFER': return '→';
      default: return '•';
    }
  }

  getTypeClass(type: string): string {
    switch (type) {
      case 'DEPOSIT': return 'text-green-600';
      case 'WITHDRAWAL': return 'text-orange-600';
      case 'TRANSFER': return 'text-blue-600';
      default: return 'text-gray-600';
    }
  }

  logout() {
    this.authService.logout();
  }
}

