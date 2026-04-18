import { Component, Inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { TaskService } from '../../../services/task.service';
import { ProjectService } from '../../../services/project.service';
import { TaskDto, TASK_STATUSES, TASK_PRIORITIES } from '../../../models/task.model';
import { ProjectDto } from '../../../models/project.model';

@Component({
  selector: 'app-task-form',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule,
    MatDialogModule, MatFormFieldModule, MatInputModule,
    MatSelectModule, MatButtonModule, MatCheckboxModule, MatProgressSpinnerModule
  ],
  templateUrl: './task-form.component.html',
  styles: [`
    .dialog-form { display: flex; flex-direction: column; gap: 4px; padding: 8px 0; min-width: 480px; }
    .full-width { width: 100%; }
    .form-row { display: flex; gap: 16px; }
    .form-row mat-form-field { flex: 1; }
  `]
})
export class TaskFormComponent implements OnInit {
  form!: FormGroup;
  loading = false;
  statuses = TASK_STATUSES;
  priorities = TASK_PRIORITIES;
  projects: ProjectDto[] = [];
  isEdit: boolean;

  constructor(
    private fb: FormBuilder,
    private taskService: TaskService,
    private projectService: ProjectService,
    private snackBar: MatSnackBar,
    private dialogRef: MatDialogRef<TaskFormComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { task: TaskDto | null }
  ) {
    this.isEdit = !!data.task;
  }

  ngOnInit(): void {
    const t = this.data.task;
    this.form = this.fb.group({
      title: [t?.title ?? '', [Validators.required, Validators.minLength(3)]],
      description: [t?.description ?? '', [Validators.required, Validators.minLength(3)]],
      taskStatusType: [t?.taskStatusType ?? 'TODO', Validators.required],
      taskPriorityType: [t?.taskPriorityType ?? 'MEDIUM', Validators.required],
      active: [t?.active ?? true],
      projectId: [t?.projectId ?? '', Validators.required]
    });

    this.projectService.getProjects(0, 100).subscribe({
      next: (page) => { this.projects = page.content; }
    });
  }

  onSubmit(): void {
    if (this.form.invalid) return;
    this.loading = true;
    const dto: TaskDto = this.form.value;

    const request = this.isEdit
      ? this.taskService.updateTask(this.data.task!.id!, dto)
      : this.taskService.createTask(dto);

    request.subscribe({
      next: (res) => { this.dialogRef.close(res.data); },
      error: (err) => {
        this.loading = false;
        this.snackBar.open(err.error?.message ?? 'Failed to save task.', 'Close', { duration: 3000 });
      }
    });
  }

  onCancel(): void {
    this.dialogRef.close(null);
  }
}
