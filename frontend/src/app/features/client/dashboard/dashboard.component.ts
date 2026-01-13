import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { AccountService } from '../../../core/services/account.service';
import { OperationService } from '../../../core/services/operation.service';
import { Account, Operation, OperationType } from '../../../core/models/operation.model';

@Component({
  selector: 'app-client-dashboard',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent implements OnInit {
  user: any;
  account: Account | null = null;
  operations: Operation[] = [];
  loading = false;
  error = '';
  success = '';

  // Operation modal
  showOperationModal = false;
  operationType: OperationType = 'DEPOSIT';
  operationForm: FormGroup;

  // Document upload
  showUploadModal = false;
  operationForUpload: Operation | null = null;
  selectedFile: File | null = null;

  constructor(
    private authService: AuthService,
    private accountService: AccountService,
    private operationService: OperationService,
    private fb: FormBuilder,
    private router: Router
  ) {
    this.operationForm = this.fb.group({
      amount: ['', [Validators.required, Validators.min(1)]],
      destinationAccountNumber: ['']
    });
  }

  ngOnInit() {
    this.user = this.authService.getCurrentUser();
    this.loadAccount();
    this.loadOperations();
  }

  loadAccount() {
    this.accountService.getMyAccount().subscribe({
      next: (account) => {
        this.account = account;
      },
      error: (err) => {
        this.error = 'Failed to load account info';
      }
    });
  }

  loadOperations() {
    this.loading = true;
    this.operationService.getMyOperations().subscribe({
      next: (operations) => {
        this.operations = operations;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to load operations';
        this.loading = false;
      }
    });
  }

  // Open operation modal
  openDepositModal() {
    this.operationType = 'DEPOSIT';
    this.operationForm.reset();
    this.operationForm.get('destinationAccountNumber')?.clearValidators();
    this.operationForm.get('destinationAccountNumber')?.updateValueAndValidity();
    this.showOperationModal = true;
  }

  openWithdrawalModal() {
    this.operationType = 'WITHDRAWAL';
    this.operationForm.reset();
    this.operationForm.get('destinationAccountNumber')?.clearValidators();
    this.operationForm.get('destinationAccountNumber')?.updateValueAndValidity();
    this.showOperationModal = true;
  }

  openTransferModal() {
    this.operationType = 'TRANSFER';
    this.operationForm.reset();
    this.operationForm.get('destinationAccountNumber')?.setValidators([Validators.required]);
    this.operationForm.get('destinationAccountNumber')?.updateValueAndValidity();
    this.showOperationModal = true;
  }

  closeOperationModal() {
    this.showOperationModal = false;
    this.operationForm.reset();
  }

  submitOperation() {
    if (this.operationForm.invalid) {
      this.operationForm.markAllAsTouched();
      return;
    }

    const amount = this.operationForm.get('amount')?.value;
    const request = {
      type: this.operationType,
      amount: amount,
      destinationAccountNumber: this.operationType === 'TRANSFER'
        ? this.operationForm.get('destinationAccountNumber')?.value
        : undefined
    };

    this.loading = true;
    this.operationService.createOperation(request).subscribe({
      next: (operation) => {
        this.success = `${this.operationType} of ${amount} DH created successfully`;
        this.closeOperationModal();
        this.loadAccount();
        this.loadOperations();

        // Show upload prompt for high amounts
        if (amount > 10000) {
          this.operationForUpload = operation;
          this.showUploadModal = true;
        }

        this.clearMessages();
      },
      error: (err) => {
        this.error = err.error?.message || 'Failed to create operation';
        this.loading = false;
      }
    });
  }

  // Document upload
  onFileSelected(event: any) {
    const file = event.target.files[0];
    if (file) {
      this.selectedFile = file;
    }
  }

  uploadDocument() {
    if (!this.operationForUpload || !this.selectedFile) return;

    this.operationService.uploadDocument(this.operationForUpload.id, this.selectedFile).subscribe({
      next: () => {
        this.success = 'Document uploaded successfully';
        this.closeUploadModal();
        this.loadOperations();
        this.clearMessages();
      },
      error: () => {
        this.error = 'Failed to upload document';
      }
    });
  }

  closeUploadModal() {
    this.showUploadModal = false;
    this.operationForUpload = null;
    this.selectedFile = null;
  }

  // upload later
  openUploadModalForOperation(operation: Operation) {
    this.operationForUpload = operation;
    this.selectedFile = null;
    this.showUploadModal = true;
  }

  skipUpload() {
    this.closeUploadModal();
  }

  getPendingCount(): number {
    return this.operations.filter(op => op.status === 'PENDING').length;
  }

  clearMessages() {
    setTimeout(() => {
      this.success = '';
      this.error = '';
    }, 3000);
  }

  getStatusClass(status: string): string {
    switch (status) {
      case 'EXECUTED': return 'bg-green-100 text-green-800';
      case 'APPROVED': return 'bg-blue-100 text-blue-800';
      case 'PENDING': return 'bg-yellow-100 text-yellow-800';
      case 'REJECTED': return 'bg-red-100 text-red-800';
      case 'CANCELLED': return 'bg-gray-100 text-gray-800';
      default: return 'bg-gray-100 text-gray-800';
    }
  }

  getTypeIcon(type: string): string {
    switch (type) {
      case 'DEPOSIT': return '↓';
      case 'WITHDRAWAL': return '↑';
      case 'TRANSFER': return '⇄';
      default: return '•';
    }
  }

  // check if transfer is outgoing or incoming
  isOutgoingTransfer(operation: Operation): boolean {
    if (!this.account || operation.type !== 'TRANSFER') return true;
    return operation.sourceAccountNumber === this.account.accountNumber;
  }

  logout() {
    this.authService.logout();
  }
}
