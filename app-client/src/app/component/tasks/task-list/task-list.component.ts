import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatCardModule } from '@angular/material/card';
import { TaskService } from '../../../services/task.service';
import { TaskDto } from '../../../models/task.model';
import { TaskFormComponent } from '../task-form/task-form.component';

@Component({
  selector: 'app-task-list',
  standalone: true,
  imports: [
    CommonModule,
    MatTableModule, MatPaginatorModule, MatButtonModule,
    MatIconModule, MatProgressSpinnerModule, MatCardModule
  ],
  templateUrl: './task-list.component.html',
  styleUrl: './task-list.component.css'
})
export class TaskListComponent implements OnInit {
  displayedColumns = ['title', 'priority', 'status', 'active', 'actions'];
  tasks: TaskDto[] = [];
  totalElements = 0;
  pageSize = 10;
  pageIndex = 0;
  loading = true;

  constructor(
    private taskService: TaskService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.loadTasks();
  }

  loadTasks(): void {
    this.loading = true;
    this.taskService.getTasks(this.pageIndex, this.pageSize).subscribe({
      next: (page) => {
        this.tasks = page.content;
        this.totalElements = page.totalElements;
        this.loading = false;
      },
      error: () => { this.loading = false; }
    });
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadTasks();
  }

  openCreateDialog(): void {
    const ref = this.dialog.open(TaskFormComponent, {
      width: '560px',
      data: { task: null }
    });
    ref.afterClosed().subscribe(result => {
      if (result) this.loadTasks();
    });
  }

  openEditDialog(task: TaskDto): void {
    const ref = this.dialog.open(TaskFormComponent, {
      width: '560px',
      data: { task }
    });
    ref.afterClosed().subscribe(result => {
      if (result) this.loadTasks();
    });
  }

  deleteTask(id: string): void {
    if (!confirm('Delete this task?')) return;
    this.taskService.deleteTask(id).subscribe({
      next: () => {
        this.snackBar.open('Task deleted.', 'Close', { duration: 3000 });
        this.loadTasks();
      },
      error: (err) => {
        const msg = err.status === 403
          ? 'You do not have permission to delete this task.'
          : 'Failed to delete task.';
        this.snackBar.open(msg, 'Close', { duration: 3000 });
      }
    });
  }
}
