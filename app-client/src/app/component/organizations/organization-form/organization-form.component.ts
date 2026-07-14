import { Component, Inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { OrganizationService } from '../../../services/organization.service';
import { OrganizationDto } from '../../../models/organization.model';

@Component({
  selector: 'app-organization-form',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule,
    MatDialogModule, MatFormFieldModule, MatInputModule,
    MatButtonModule, MatProgressSpinnerModule
  ],
  templateUrl: './organization-form.component.html',
  styles: [`
    .dialog-form { display: flex; flex-direction: column; gap: 4px; padding: 8px 0; min-width: 420px; }
    .full-width { width: 100%; }
  `]
})
export class OrganizationFormComponent implements OnInit {
  form!: FormGroup;
  loading = false;
  isEdit: boolean;

  constructor(
    private fb: FormBuilder,
    private organizationService: OrganizationService,
    private snackBar: MatSnackBar,
    private dialogRef: MatDialogRef<OrganizationFormComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { organization: OrganizationDto | null }
  ) {
    this.isEdit = !!data.organization;
  }

  ngOnInit(): void {
    const o = this.data.organization;
    this.form = this.fb.group({
      name: [o?.name ?? '', [Validators.required, Validators.minLength(3)]],
      description: [o?.description ?? '', [Validators.maxLength(1000)]]
    });
  }

  onSubmit(): void {
    if (this.form.invalid) return;
    this.loading = true;
    const v = this.form.value;
    const dto: OrganizationDto = {
      name: v.name,
      description: v.description
    };

    const request = this.isEdit
      ? this.organizationService.updateOrganization(this.data.organization!.id!, { ...dto, id: this.data.organization!.id })
      : this.organizationService.createOrganization(dto);

    request.subscribe({
      next: (res) => { this.dialogRef.close(res.data); },
      error: (err) => {
        this.loading = false;
        this.snackBar.open(err.error?.message ?? 'Failed to save organization.', 'Close', { duration: 3000 });
      }
    });
  }

  onCancel(): void {
    this.dialogRef.close(null);
  }
}
