import { Component } from '@angular/core';

import { NgIf } from '@angular/common';

import { Router, RouterLink } from '@angular/router';

import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';

import { MatInput } from '@angular/material/input';
import { MatButton } from '@angular/material/button';
import { MatProgressSpinner } from '@angular/material/progress-spinner';
import { MatCard, MatCardContent, MatCardTitle } from '@angular/material/card';
import { MatError, MatFormField, MatLabel } from '@angular/material/form-field';

import { MatSnackBar } from '@angular/material/snack-bar';

import { AuthUserService } from '../../../services/authuser.service';

@Component({
  selector: 'app-sign-in',
  standalone: true,
  imports: [
    MatProgressSpinner,
    NgIf,
    RouterLink,
    MatButton,
    MatLabel,
    MatError,
    MatCardTitle,
    MatCard,
    MatCardContent,
    ReactiveFormsModule,
    MatInput,
    MatFormField
  ],
  templateUrl: './sign-in.component.html',
  styleUrl: './sign-in.component.css'
})
export class SignInComponent {

  signInForm!: FormGroup;
  isLoading = false;
  redirecting = false;

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private authUserService: AuthUserService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.signInForm = this.fb.group({
      username: ['', Validators.required],
      password: ['', Validators.required]
    });
  }

  onSubmit(): void {
    if (this.signInForm.valid) {
      this.isLoading = true;
      const formValues = this.signInForm.value;
      this.authUserService.signIn(formValues).subscribe({
        next: (response) => {
          this.isLoading = true;
          this.redirecting = true;
          setTimeout(() => {
            void this.router.navigate(['/dashboard']);
          }, 2000);
        },
        error: (error) => {
          this.isLoading = false;
          this.snackBar.open('Sign-in failed. Please check your credentials.', 'Close', {
            duration: 3000,
            verticalPosition: 'top',
            horizontalPosition: 'center',
          });
        },
      });
    }
  }
}
