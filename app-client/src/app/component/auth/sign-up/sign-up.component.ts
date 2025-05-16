import { Component } from '@angular/core';
import { NgIf } from '@angular/common';

import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';

import { MatCard, MatCardContent, MatCardTitle } from '@angular/material/card';
import { MatLabel, MatFormField } from '@angular/material/form-field';
import { MatInput } from '@angular/material/input';
import { MatButton } from '@angular/material/button';
import { MatError } from '@angular/material/form-field';

import { AuthUserService } from "../../../services/authuser.service";
import { MatSnackBar } from "@angular/material/snack-bar";
import { MatProgressSpinner } from "@angular/material/progress-spinner";

@Component({
  selector: 'app-sign-up',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    NgIf,
    NgIf,
    MatError,
    MatLabel,
    MatFormField,
    MatInput,
    MatButton,
    MatCardContent,
    MatCardTitle,
    MatCard,
    MatInput,
    MatFormField,
    MatCardContent,
    MatCardTitle,
    MatCard,
    MatButton,
    MatProgressSpinner
  ],
  templateUrl: './sign-up.component.html',
  styleUrl: './sign-up.component.css'
})
export class SignUpComponent {

  signUpForm!: FormGroup;
  isLoading = false;

  constructor(
    private fb: FormBuilder,
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
          console.log('Success:', response);
        },
        error: (error) => {
          console.error('Error:', error);
        }
      });
    }
  }
}
