import { Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { UserService } from '../../services/user.service';
import { AuthTokenService } from '../../services/auth-token.service';
import { UserDto } from '../../models/user.model';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule,
    MatFormFieldModule, MatInputModule, MatButtonModule,
    MatCardModule, MatProgressSpinnerModule, MatIconModule
  ],
  templateUrl: './profile.component.html',
  styleUrl: './profile.component.css'
})
export class ProfileComponent implements OnInit {
  @ViewChild('fileInput') fileInputRef!: ElementRef<HTMLInputElement>;

  form!: FormGroup;
  loading = true;
  saving = false;
  uploading = false;
  deleting = false;
  currentUser: UserDto | null = null;
  username: string;

  constructor(
    private fb: FormBuilder,
    private userService: UserService,
    private authTokenService: AuthTokenService,
    private snackBar: MatSnackBar
  ) {
    this.username = authTokenService.getUsername() ?? '';
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      firstName: ['', [Validators.required, Validators.minLength(3)]],
      lastName: ['', [Validators.required, Validators.minLength(3)]],
      email: ['', [Validators.required, Validators.email]]
    });

    this.loadCurrentUser();
  }

  private loadCurrentUser(): void {
    const authUserId = this.authTokenService.getAuthUserId();
    if (!authUserId) {
      this.loading = false;
      return;
    }
    this.userService.getUsers(0, 100).subscribe({
      next: (page) => {
        this.currentUser = page.content.find(u => u.authUserId === authUserId) ?? null;
        if (this.currentUser) {
          this.form.patchValue({
            firstName: this.currentUser.firstName,
            lastName: this.currentUser.lastName,
            email: this.currentUser.email
          });
        }
        this.loading = false;
      },
      error: () => { this.loading = false; }
    });
  }

  onSubmit(): void {
    if (this.form.invalid || !this.currentUser) return;
    this.saving = true;
    const dto: UserDto = { ...this.currentUser, ...this.form.value };
    this.userService.updateUser(this.currentUser.id, dto).subscribe({
      next: (updated) => {
        this.currentUser = updated;
        this.saving = false;
        this.snackBar.open('Profile updated successfully.', 'Close', { duration: 3000 });
      },
      error: (err) => {
        this.saving = false;
        const msg = err.status === 403
          ? 'You do not have permission to update this profile.'
          : 'Failed to update profile.';
        this.snackBar.open(msg, 'Close', { duration: 3000 });
      }
    });
  }

  onAvatarImageError(): void {
    if (this.currentUser) {
      this.currentUser = { ...this.currentUser, avatarUrl: null };
    }
  }

  onAvatarClick(): void {
    if (!this.currentUser || this.uploading) return;
    this.fileInputRef.nativeElement.click();
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files?.length || !this.currentUser) return;
    const file = input.files[0];
    input.value = '';
    this.uploading = true;
    this.userService.uploadAvatar(this.currentUser.id, file).subscribe({
      next: (res) => {
        this.currentUser = { ...this.currentUser!, avatarUrl: res.avatarUrl };
        this.uploading = false;
        this.snackBar.open('Avatar updated successfully.', 'Close', { duration: 3000 });
      },
      error: (err) => {
        this.uploading = false;
        const msg = err.status === 422
          ? 'Invalid file. Only JPEG, PNG, or WebP images up to 5 MB are accepted.'
          : err.status === 403
          ? 'You do not have permission to update this avatar.'
          : 'Failed to upload avatar.';
        this.snackBar.open(msg, 'Close', { duration: 4000 });
      }
    });
  }

  onDeleteAvatar(): void {
    if (!this.currentUser?.avatarUrl || this.deleting) return;
    this.deleting = true;
    this.userService.deleteAvatar(this.currentUser.id).subscribe({
      next: () => {
        this.currentUser = { ...this.currentUser!, avatarUrl: null };
        this.deleting = false;
        this.snackBar.open('Avatar removed.', 'Close', { duration: 3000 });
      },
      error: (err) => {
        this.deleting = false;
        const msg = err.status === 403
          ? 'You do not have permission to remove this avatar.'
          : 'Failed to remove avatar.';
        this.snackBar.open(msg, 'Close', { duration: 3000 });
      }
    });
  }
}
