import { Component, OnDestroy, OnInit } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { MatSidenav, MatSidenavContainer, MatSidenavContent } from '@angular/material/sidenav';
import { MatIcon } from '@angular/material/icon';
import { MatIconButton } from '@angular/material/button';
import { BreakpointObserver, Breakpoints } from '@angular/cdk/layout';
import { Subscription } from 'rxjs';
import { AuthTokenService } from '../../../services/auth-token.service';

@Component({
  selector: 'app-shell',
  standalone: true,
  imports: [
    RouterOutlet, RouterLink, RouterLinkActive,
    MatSidenavContainer, MatSidenav, MatSidenavContent,
    MatIcon, MatIconButton
  ],
  templateUrl: './shell.component.html',
  styleUrl: './shell.component.css'
})
export class ShellComponent implements OnInit, OnDestroy {
  username = '';
  initials = '';
  sidenavOpened = true;
  sidenavMode: 'side' | 'over' = 'side';
  isMobile = false;
  private sub!: Subscription;

  constructor(
    private authTokenService: AuthTokenService,
    private router: Router,
    private breakpointObserver: BreakpointObserver
  ) {
    this.username = authTokenService.getUsername() ?? '';
    this.initials = this.username ? this.username.charAt(0).toUpperCase() : 'U';
  }

  ngOnInit(): void {
    this.sub = this.breakpointObserver
      .observe([Breakpoints.Handset])
      .subscribe(result => {
        this.isMobile = result.matches;
        this.sidenavMode = result.matches ? 'over' : 'side';
        this.sidenavOpened = !result.matches;
      });
  }

  ngOnDestroy(): void {
    this.sub?.unsubscribe();
  }

  toggleSidenav(): void {
    this.sidenavOpened = !this.sidenavOpened;
  }

  closeSidenavOnMobile(): void {
    if (this.isMobile) this.sidenavOpened = false;
  }

  logout(): void {
    this.authTokenService.logout();
    void this.router.navigate(['/sign-in']);
  }
}
