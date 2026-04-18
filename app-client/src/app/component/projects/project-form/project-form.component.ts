import { Component, Inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { ProjectService } from '../../../services/project.service';
import { ProjectDto, PROJECT_STATUSES } from '../../../models/project.model';

@Component({
  selector: 'app-project-form',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule,
    MatDialogModule, MatFormFieldModule, MatInputModule,
    MatSelectModule, MatButtonModule, MatProgressSpinnerModule
  ],
  templateUrl: './project-form.component.html',
  styles: [`
    .dialog-form { display: flex; flex-direction: column; gap: 4px; padding: 8px 0; min-width: 480px; }
    .full-width { width: 100%; }
    .form-row { display: flex; gap: 16px; }
    .form-row mat-form-field { flex: 1; }
    .ai-btn { margin-bottom: 8px; }
  `]
})
export class ProjectFormComponent implements OnInit {
  form!: FormGroup;
  loading = false;
  generating = false;
  statuses = PROJECT_STATUSES;
  isEdit: boolean;

  constructor(
    private fb: FormBuilder,
    private projectService: ProjectService,
    private snackBar: MatSnackBar,
    private dialogRef: MatDialogRef<ProjectFormComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { project: ProjectDto | null }
  ) {
    this.isEdit = !!data.project;
  }

  ngOnInit(): void {
    const p = this.data.project;
    this.form = this.fb.group({
      title: [p?.title ?? '', [Validators.required, Validators.minLength(3)]],
      description: [p?.description ?? '', [Validators.required, Validators.minLength(3)]],
      projectStatusType: [p?.projectStatusType ?? 'PLANNING', Validators.required],
      startDate: [p ? this.toInputDate(p.startDate) : '', Validators.required],
      endDate: [p ? this.toInputDate(p.endDate) : '', Validators.required]
    });
  }

  generateDescription(): void {
    const title = this.form.get('title')?.value?.trim();
    if (!title || !this.data.project?.id) {
      this.snackBar.open('Save the project first, then generate a description.', 'Close', { duration: 3000 });
      return;
    }
    this.generating = true;
    this.projectService.generateDescription(this.data.project.id, title).subscribe({
      next: (res) => {
        this.form.get('description')?.setValue(res.data);
        this.generating = false;
      },
      error: () => {
        this.snackBar.open('Failed to generate description.', 'Close', { duration: 3000 });
        this.generating = false;
      }
    });
  }

  onSubmit(): void {
    if (this.form.invalid) return;
    this.loading = true;
    const value = this.form.value;
    const dto: ProjectDto = {
      ...value,
      startDate: this.toApiDate(value.startDate),
      endDate: this.toApiDate(value.endDate)
    };

    const request = this.isEdit
      ? this.projectService.updateProject(this.data.project!.id!, { ...dto, id: this.data.project!.id, userId: this.data.project!.userId })
      : this.projectService.createProject(dto);

    request.subscribe({
      next: (res) => { this.dialogRef.close(res.data); },
      error: (err) => {
        this.loading = false;
        this.snackBar.open(err.error?.message ?? 'Failed to save project.', 'Close', { duration: 3000 });
      }
    });
  }

  onCancel(): void {
    this.dialogRef.close(null);
  }

  private toInputDate(dateStr: string): string {
    return dateStr ? dateStr.substring(0, 16) : '';
  }

  private toApiDate(inputDate: string): string {
    return inputDate ? inputDate + ':00' : '';
  }
}
