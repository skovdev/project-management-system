import { Component } from '@angular/core';

import { NgIf } from '@angular/common';

import {NavigationEnd, Router, RouterLink} from '@angular/router';

import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';

import { MatInput } from '@angular/material/input';
import { MatButton } from '@angular/material/button';
import { MatProgressSpinner } from '@angular/material/progress-spinner';
import { MatCard, MatCardContent, MatCardTitle } from '@angular/material/card';
import { MatError, MatFormField, MatLabel } from '@angular/material/form-field';

import { MatSnackBar } from '@angular/material/snack-bar';

import { AuthUserService } from '../../../services/auth-user.service';
import { AuthTokenService } from '../../../services/auth-token.service';
import {filter, take} from "rxjs";

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
    if (this.signInForm.valid) {
      const formData = this.signInForm.value;

      this.authUserService.signIn(formData).subscribe({
        next: (response) => {
          void this.router.navigate(['/dashboard']);
        },
        error: (error) => {
          console.error('Sign-in error:', error);
          this.showErrorMessage('Sign-in failed. Please check your credentials and try again.');
        }
      });
    }
  }

  private showErrorMessage(message: string): void {
    this.snackBar.open(message, 'Close', {
      duration: 3000,
      verticalPosition: 'top',
      horizontalPosition: 'center'
    });
  }
}
