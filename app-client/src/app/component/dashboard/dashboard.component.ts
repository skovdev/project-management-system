import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { forkJoin } from 'rxjs';
import { MatCard, MatCardContent } from '@angular/material/card';
import { MatProgressSpinner } from '@angular/material/progress-spinner';
import { ProjectService } from '../../services/project.service';
import { TaskService } from '../../services/task.service';
import { ProjectDto } from '../../models/project.model';
import { TaskDto } from '../../models/task.model';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, MatCard, MatCardContent, MatProgressSpinner],
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
    private router: Router
  ) {}

  ngOnInit(): void {
    forkJoin({
      projects: this.projectService.getProjects(0, 5),
      tasks: this.taskService.getTasks(0, 5)
    }).subscribe({
      next: ({ projects, tasks }) => {
        this.totalProjects = projects.totalElements;
        this.totalTasks = tasks.totalElements;
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
