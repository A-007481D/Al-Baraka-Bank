import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { UserService } from '../../../core/services/user.service';
import { UserResponse, UserRequest, UserRole } from '../../../core/models/user.model';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent implements OnInit {
  user: any;
  users: UserResponse[] = [];
  loading = false;
  error = '';
  success = '';

  // Modal state
  showModal = false;
  isEditing = false;
  editingUserId: number | null = null;
  userForm: FormGroup;

  // Delete confirmation
  showDeleteConfirm = false;
  userToDelete: UserResponse | null = null;

  constructor(
    private authService: AuthService,
    private userService: UserService,
    private fb: FormBuilder,
    private router: Router
  ) {
    this.userForm = this.fb.group({
      fullName: ['', [Validators.required, Validators.minLength(2)]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.minLength(6)]],
      role: ['CLIENT', Validators.required],
      active: [true]
    });
  }

  ngOnInit() {
    this.user = this.authService.getCurrentUser();
    this.loadUsers();
  }

  loadUsers() {
    this.loading = true;
    this.error = '';
    this.userService.getAllUsers().subscribe({
      next: (users) => {
        this.users = users;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to load users';
        this.loading = false;
      }
    });
  }

  // Modal actions
  openCreateModal() {
    this.isEditing = false;
    this.editingUserId = null;
    this.userForm.reset({ role: 'CLIENT', active: true });
    this.userForm.get('password')?.setValidators([Validators.required, Validators.minLength(6)]);
    this.userForm.get('password')?.updateValueAndValidity();
    this.showModal = true;
  }

  openEditModal(user: UserResponse) {
    this.isEditing = true;
    this.editingUserId = user.id;
    this.userForm.patchValue({
      fullName: user.fullName,
      email: user.email,
      password: '',
      role: user.role,
      active: user.active
    });
    // Password is optional when editing
    this.userForm.get('password')?.clearValidators();
    this.userForm.get('password')?.setValidators([Validators.minLength(6)]);
    this.userForm.get('password')?.updateValueAndValidity();
    this.showModal = true;
  }

  closeModal() {
    this.showModal = false;
    this.userForm.reset();
  }

  submitForm() {
    if (this.userForm.invalid) {
      this.userForm.markAllAsTouched();
      return;
    }

    const formValue = this.userForm.value;
    const userData: UserRequest = {
      fullName: formValue.fullName,
      email: formValue.email,
      role: formValue.role,
      active: formValue.active
    };

    // Only include password if provided
    if (formValue.password) {
      userData.password = formValue.password;
    }

    this.loading = true;

    if (this.isEditing && this.editingUserId) {
      this.userService.updateUser(this.editingUserId, userData).subscribe({
        next: () => {
          this.success = 'User updated successfully';
          this.closeModal();
          this.loadUsers();
          this.clearMessages();
        },
        error: (err) => {
          this.error = err.error?.message || 'Failed to update user';
          this.loading = false;
        }
      });
    } else {
      this.userService.createUser(userData).subscribe({
        next: () => {
          this.success = 'User created successfully';
          this.closeModal();
          this.loadUsers();
          this.clearMessages();
        },
        error: (err) => {
          this.error = err.error?.message || 'Failed to create user';
          this.loading = false;
        }
      });
    }
  }

  // User actions
  toggleUserStatus(user: UserResponse) {
    const action = user.active
      ? this.userService.deactivateUser(user.id)
      : this.userService.activateUser(user.id);

    action.subscribe({
      next: () => {
        this.success = `User ${user.active ? 'deactivated' : 'activated'} successfully`;
        this.loadUsers();
        this.clearMessages();
      },
      error: () => {
        this.error = 'Failed to update user status';
      }
    });
  }

  confirmDelete(user: UserResponse) {
    this.userToDelete = user;
    this.showDeleteConfirm = true;
  }

  cancelDelete() {
    this.showDeleteConfirm = false;
    this.userToDelete = null;
  }

  deleteUser() {
    if (!this.userToDelete) return;

    this.userService.deleteUser(this.userToDelete.id).subscribe({
      next: () => {
        this.success = 'User deleted successfully';
        this.showDeleteConfirm = false;
        this.userToDelete = null;
        this.loadUsers();
        this.clearMessages();
      },
      error: () => {
        this.error = 'Failed to delete user';
        this.showDeleteConfirm = false;
      }
    });
  }

  clearMessages() {
    setTimeout(() => {
      this.success = '';
      this.error = '';
    }, 3000);
  }

  getRoleBadgeClass(role: UserRole): string {
    switch (role) {
      case 'ADMIN': return 'bg-purple-100 text-purple-800';
      case 'AGENT_BANCAIRE': return 'bg-blue-100 text-blue-800';
      case 'CLIENT': return 'bg-green-100 text-green-800';
      default: return 'bg-gray-100 text-gray-800';
    }
  }

  logout() {
    this.authService.logout();
  }
}
