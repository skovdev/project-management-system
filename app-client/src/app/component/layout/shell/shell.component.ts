import { Component } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { MatToolbar } from '@angular/material/toolbar';
import { MatSidenav, MatSidenavContainer, MatSidenavContent } from '@angular/material/sidenav';
import { MatNavList, MatListItem } from '@angular/material/list';
import { MatIcon } from '@angular/material/icon';
import { MatIconButton } from '@angular/material/button';
import { AuthTokenService } from '../../../services/auth-token.service';

@Component({
  selector: 'app-shell',
  standalone: true,
  imports: [
    RouterOutlet, RouterLink, RouterLinkActive,
    MatToolbar, MatSidenavContainer, MatSidenav, MatSidenavContent,
    MatNavList, MatListItem, MatIcon, MatIconButton
  ],
  templateUrl: './shell.component.html',
  styleUrl: './shell.component.css'
})
export class ShellComponent {
  username = '';

  constructor(
    private authTokenService: AuthTokenService,
    private router: Router
  ) {
    this.username = authTokenService.getUsername() ?? '';
  }

  logout(): void {
    this.authTokenService.logout();
    void this.router.navigate(['/sign-in']);
  }
}
