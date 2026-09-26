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
import { MatDatepickerModule } from '@angular/material/datepicker';
import { provideNativeDateAdapter } from '@angular/material/core';
import { TaskService } from '../../../services/task.service';
import { ProjectService } from '../../../services/project.service';
import { OrganizationService } from '../../../services/organization.service';
import { UserService } from '../../../services/user.service';
import { CurrentOrganizationService } from '../../../services/current-organization.service';
import { TaskDto, TASK_STATUSES, TASK_PRIORITIES } from '../../../models/task.model';
import { ProjectDto } from '../../../models/project.model';
import { OrganizationMemberDto } from '../../../models/organization.model';
import { UserDto } from '../../../models/user.model';

@Component({
  selector: 'app-task-form',
  standalone: true,
  providers: [provideNativeDateAdapter()],
  imports: [
    CommonModule, ReactiveFormsModule,
    MatDialogModule, MatFormFieldModule, MatInputModule,
    MatSelectModule, MatButtonModule, MatCheckboxModule, MatProgressSpinnerModule,
    MatDatepickerModule
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
  members: OrganizationMemberDto[] = [];
  usersById = new Map<string, UserDto>();
  isEdit: boolean;

  constructor(
    private fb: FormBuilder,
    private taskService: TaskService,
    private projectService: ProjectService,
    private organizationService: OrganizationService,
    private userService: UserService,
    private currentOrganizationService: CurrentOrganizationService,
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
      projectId: [t?.projectId ?? '', Validators.required],
      assigneeId: [t?.assigneeId ?? null],
      dueDatePart: [this.parseDate(t?.dueDate)]
    });

    const organizationId = this.currentOrganizationService.getCurrentOrganizationId();
    if (organizationId) {
      this.projectService.getProjects(organizationId, 0, 100).subscribe({
        next: (page) => { this.projects = page.content; }
      });
      this.userService.getUsers(0, 1000).subscribe({
        next: (page) => {
          this.usersById = new Map(page.content.map(u => [u.authUserId, u]));
        }
      });
      this.organizationService.getMembers(organizationId, 0, 1000).subscribe({
        next: (page) => { this.members = page.content; }
      });
    }
  }

  memberDisplayName(userId: string): string {
    const u = this.usersById.get(userId);
    return u ? `${u.firstName} ${u.lastName} (${u.email})` : userId;
  }

  onSubmit(): void {
    if (this.form.invalid) return;
    this.loading = true;
    const v = this.form.value;
    const dto: TaskDto = {
      title: v.title,
      description: v.description,
      taskStatusType: v.taskStatusType,
      taskPriorityType: v.taskPriorityType,
      active: v.active,
      projectId: v.projectId,
      assigneeId: v.assigneeId ?? undefined,
      dueDate: this.combineDate(v.dueDatePart)
    };

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

  private parseDate(dateStr: string | undefined): Date | null {
    if (!dateStr) return null;
    const [year, month, day] = dateStr.split('T')[0].split('-').map(Number);
    return new Date(year, month - 1, day);
  }

  private combineDate(date: Date | null): string | undefined {
    if (!date) return undefined;
    const y = date.getFullYear();
    const m = String(date.getMonth() + 1).padStart(2, '0');
    const d = String(date.getDate()).padStart(2, '0');
    return `${y}-${m}-${d}T00:00:00`;
  }
}
