import { Routes } from '@angular/router';

import { SignUpComponent } from './component/auth/sign-up/sign-up.component';
import { DashboardComponent } from './component/dashboard/dashboard.component';
import { SignInComponent } from './component/auth/sign-in/sign-in.component';

export const routes: Routes = [
  { path: 'sign-up', component: SignUpComponent },
  { path: 'sign-in', component: SignInComponent },
  { path: 'dashboard', component: DashboardComponent },
  { path: '**', redirectTo: 'sign-up' }
];
