import { Routes } from '@angular/router';
import { SignUpComponent } from './component/auth/sign-up/sign-up.component';
import { SignInComponent } from './component/auth/sign-in/sign-in.component';
import { ShellComponent } from './component/layout/shell/shell.component';
import { DashboardComponent } from './component/dashboard/dashboard.component';
import { ProjectListComponent } from './component/projects/project-list/project-list.component';
import { TaskListComponent } from './component/tasks/task-list/task-list.component';
import { ProfileComponent } from './component/profile/profile.component';
import { authGuard } from './guards/auth.guard';

export const routes: Routes = [
  { path: 'sign-up', component: SignUpComponent },
  { path: 'sign-in', component: SignInComponent },
  {
    path: '',
    component: ShellComponent,
    canActivate: [authGuard],
    children: [
      { path: 'dashboard', component: DashboardComponent },
      { path: 'projects', component: ProjectListComponent },
      { path: 'tasks', component: TaskListComponent },
      { path: 'profile', component: ProfileComponent },
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' }
    ]
  },
  { path: '**', redirectTo: 'sign-in' }
];
