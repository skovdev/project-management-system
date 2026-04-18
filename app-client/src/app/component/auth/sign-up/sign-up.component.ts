import { Component, OnInit } from '@angular/core';
import { NgIf } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { MatInput } from '@angular/material/input';
import { MatButton } from '@angular/material/button';
import { MatProgressSpinner } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatCard, MatCardContent, MatCardTitle } from '@angular/material/card';
import { MatFormField, MatLabel, MatError } from '@angular/material/form-field';
import { AuthUserService } from '../../../services/auth-user.service';

@Component({
  selector: 'app-sign-up',
  standalone: true,
  imports: [
    NgIf, ReactiveFormsModule, RouterLink,
    MatFormField, MatLabel, MatInput, MatError, MatButton,
    MatCard, MatCardContent, MatCardTitle, MatProgressSpinner, MatSnackBarModule
  ],
  templateUrl: './sign-up.component.html',
  styleUrl: './sign-up.component.css'
})
export class SignUpComponent implements OnInit {
  signUpForm!: FormGroup;
  loading = false;

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
      password: ['', [Validators.required, Validators.minLength(6)]]
    });
  }

  onSubmit(): void {
    if (this.signUpForm.invalid) return;
    this.loading = true;
    this.authUserService.signUp(this.signUpForm.value).subscribe({
      next: () => {
        this.loading = false;
        this.snackBar.open('Account created! Please sign in.', 'Close', {
          duration: 4000,
          verticalPosition: 'top'
        });
        void this.router.navigate(['/sign-in']);
      },
      error: () => {
        this.loading = false;
        this.snackBar.open('Username or email already exists.', 'Close', {
          duration: 3000,
          verticalPosition: 'top'
        });
      }
    });
  }
}
