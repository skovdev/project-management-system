import { Component } from '@angular/core';

import { NgIf } from '@angular/common';

import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';

import { Router, RouterLink } from '@angular/router';

import { MatInput } from '@angular/material/input';
import { MatButton } from '@angular/material/button';
import { MatProgressSpinner } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatCard, MatCardContent, MatCardTitle } from '@angular/material/card';
import { MatFormField, MatLabel, MatError } from '@angular/material/form-field';

import { AuthUserService } from '../../../services/authuser.service';

@Component({
  selector: 'app-sign-up',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    NgIf,
    MatFormField,
    MatLabel,
    MatInput,
    MatError,
    MatButton,
    MatCard,
    MatCardContent,
    MatCardTitle,
    MatProgressSpinner,
    MatSnackBarModule,
    RouterLink
  ],
  templateUrl: './sign-up.component.html',
  styleUrl: './sign-up.component.css'
})
export class SignUpComponent {

  signUpForm!: FormGroup;
  isLoading = false;
  redirecting = false;

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private authUserService: AuthUserService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.signUpForm = this.fb.group({
      username: ['', [Validators.required, Validators.minLength(4)]],
      firstName: ['', Validators.required],
      lastName: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
    });
  }

  onSubmit() {
    if (this.signUpForm.valid) {
      this.isLoading = true;
      const formValues = this.signUpForm.value;

      this.authUserService.signUp(formValues).subscribe({
        next: (response) => {
          this.isLoading = true;
          this.redirecting = true;
          setTimeout(() => {
            void this.router.navigate(['/dashboard']);
          }, 2000);
        },
        error: () => {
          this.isLoading = false;
          this.snackBar.open('The user with this username already exists.', 'Close', {
            duration: 3000,
            verticalPosition: 'top',
            horizontalPosition: 'center',
          });
        }
      });
    }
  }
}
