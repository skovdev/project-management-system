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
import { ProjectService } from '../../../services/project.service';
import { ProjectDto } from '../../../models/project.model';
import { ProjectFormComponent } from '../project-form/project-form.component';

@Component({
  selector: 'app-project-list',
  standalone: true,
  imports: [
    CommonModule,
    MatTableModule, MatPaginatorModule, MatButtonModule,
    MatIconModule, MatProgressSpinnerModule, MatCardModule
  ],
  templateUrl: './project-list.component.html',
  styleUrl: './project-list.component.css'
})
export class ProjectListComponent implements OnInit {
  displayedColumns = ['title', 'status', 'startDate', 'endDate', 'actions'];
  projects: ProjectDto[] = [];
  totalElements = 0;
  pageSize = 10;
  pageIndex = 0;
  loading = true;

  constructor(
    private projectService: ProjectService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.loadProjects();
  }

  loadProjects(): void {
    this.loading = true;
    this.projectService.getProjects(this.pageIndex, this.pageSize).subscribe({
      next: (page) => {
        this.projects = page.content;
        this.totalElements = page.totalElements;
        this.loading = false;
      },
      error: () => { this.loading = false; }
    });
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadProjects();
  }

  openCreateDialog(): void {
    const ref = this.dialog.open(ProjectFormComponent, {
      width: '560px',
      data: { project: null }
    });
    ref.afterClosed().subscribe(result => {
      if (result) this.loadProjects();
    });
  }

  openEditDialog(project: ProjectDto): void {
    const ref = this.dialog.open(ProjectFormComponent, {
      width: '560px',
      data: { project }
    });
    ref.afterClosed().subscribe(result => {
      if (result) this.loadProjects();
    });
  }

  deleteProject(id: string): void {
    if (!confirm('Delete this project?')) return;
    this.projectService.deleteProject(id).subscribe({
      next: () => {
        this.snackBar.open('Project deleted.', 'Close', { duration: 3000 });
        this.loadProjects();
      },
      error: (err) => {
        const msg = err.status === 403
          ? 'You do not have permission to delete this project.'
          : 'Failed to delete project.';
        this.snackBar.open(msg, 'Close', { duration: 3000 });
      }
    });
  }
}
