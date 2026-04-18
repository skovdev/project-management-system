import { Component, OnInit } from '@angular/core';
import { NgIf } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatInput } from '@angular/material/input';
import { MatButton } from '@angular/material/button';
import { MatProgressSpinner } from '@angular/material/progress-spinner';
import { MatCard, MatCardContent, MatCardTitle } from '@angular/material/card';
import { MatError, MatFormField, MatLabel } from '@angular/material/form-field';
import { MatSnackBar } from '@angular/material/snack-bar';
import { AuthUserService } from '../../../services/auth-user.service';
import { AuthTokenService } from '../../../services/auth-token.service';

@Component({
  selector: 'app-sign-in',
  standalone: true,
  imports: [
    NgIf, RouterLink, ReactiveFormsModule,
    MatProgressSpinner, MatButton, MatLabel, MatError,
    MatCardTitle, MatCard, MatCardContent, MatInput, MatFormField
  ],
  templateUrl: './sign-in.component.html',
  styleUrl: './sign-in.component.css'
})
export class SignInComponent implements OnInit {
  signInForm!: FormGroup;
  loading = false;

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private authUserService: AuthUserService,
    private authTokenService: AuthTokenService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.signInForm = this.fb.group({
      username: ['', Validators.required],
      password: ['', Validators.required]
    });
  }

  onSubmit(): void {
    if (this.signInForm.invalid) return;
    this.loading = true;
    this.authUserService.signIn(this.signInForm.value).subscribe({
      next: (response) => {
        const { token, authUserId, username } = response.data;
        this.authTokenService.setAuthData(token, authUserId, username);
        void this.router.navigate(['/dashboard']);
      },
      error: () => {
        this.loading = false;
        this.snackBar.open('Invalid username or password.', 'Close', {
          duration: 3000,
          verticalPosition: 'top'
        });
      }
    });
  }
}
