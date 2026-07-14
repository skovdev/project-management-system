import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { forkJoin, of } from 'rxjs';
import { MatProgressSpinner } from '@angular/material/progress-spinner';
import { MatIcon } from '@angular/material/icon';
import { ProjectService } from '../../services/project.service';
import { TaskService } from '../../services/task.service';
import { CurrentOrganizationService } from '../../services/current-organization.service';
import { ProjectDto } from '../../models/project.model';
import { Page } from '../../models/page.model';
import { TaskDto } from '../../models/task.model';

const EMPTY_PROJECT_PAGE: Page<ProjectDto> = {
  content: [],
  page: { size: 0, number: 0, totalElements: 0, totalPages: 0 }
};

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, MatProgressSpinner, MatIcon],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent implements OnInit {
  totalProjects = 0;
  totalTasks = 0;
  recentProjects: ProjectDto[] = [];
  recentTasks: TaskDto[] = [];
  loading = true;

  constructor(
    private projectService: ProjectService,
    private taskService: TaskService,
    private currentOrganizationService: CurrentOrganizationService,
    private router: Router
  ) {}

  ngOnInit(): void {
    const organizationId = this.currentOrganizationService.getCurrentOrganizationId();
    forkJoin({
      projects: organizationId ? this.projectService.getProjects(organizationId, 0, 5) : of(EMPTY_PROJECT_PAGE),
      tasks: this.taskService.getTasks(0, 5)
    }).subscribe({
      next: ({ projects, tasks }) => {
        this.totalProjects = projects.page.totalElements;
        this.totalTasks = tasks.page.totalElements;
        this.recentProjects = projects.content;
        this.recentTasks = tasks.content;
        this.loading = false;
      },
      error: () => { this.loading = false; }
    });
  }

  navigateTo(path: string): void {
    void this.router.navigate([path]);
  }
}
