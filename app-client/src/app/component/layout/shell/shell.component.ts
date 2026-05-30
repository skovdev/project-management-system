import { Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { MatSidenav, MatSidenavContainer, MatSidenavContent } from '@angular/material/sidenav';
import { MatIcon } from '@angular/material/icon';
import { MatIconButton } from '@angular/material/button';
import { MatBadgeModule } from '@angular/material/badge';
import { MatMenuModule } from '@angular/material/menu';
import { BreakpointObserver, Breakpoints } from '@angular/cdk/layout';
import { interval, Subscription } from 'rxjs';
import { startWith, switchMap } from 'rxjs/operators';
import { AuthTokenService } from '../../../services/auth-token.service';
import { NotificationService } from '../../../services/notification.service';
import { NotificationDto } from '../../../models/notification.model';

@Component({
  selector: 'app-shell',
  standalone: true,
  imports: [
    CommonModule,
    RouterOutlet, RouterLink, RouterLinkActive,
    MatSidenavContainer, MatSidenav, MatSidenavContent,
    MatIcon, MatIconButton,
    MatBadgeModule, MatMenuModule
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

  unreadCount = 0;
  unreadNotifications: NotificationDto[] = [];

  private sub!: Subscription;
  private notifSub!: Subscription;

  constructor(
    private authTokenService: AuthTokenService,
    private router: Router,
    private breakpointObserver: BreakpointObserver,
    private notificationService: NotificationService
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

    // Poll for unread notifications every 30 seconds
    this.notifSub = interval(30_000).pipe(
      startWith(0),
      switchMap(() => this.notificationService.getUnread())
    ).subscribe({
      next: (res) => {
        this.unreadNotifications = res.data ?? [];
        this.unreadCount = this.unreadNotifications.length;
      },
      error: () => {
        this.unreadNotifications = [];
        this.unreadCount = 0;
      }
    });
  }

  ngOnDestroy(): void {
    this.sub?.unsubscribe();
    this.notifSub?.unsubscribe();
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

  onNotifClick(notification: NotificationDto): void {
    if (!notification.read) {
      this.notificationService.markAsRead(notification.id).subscribe({
        next: () => {
          notification.read = true;
          this.unreadCount = Math.max(0, this.unreadCount - 1);
          this.unreadNotifications = this.unreadNotifications.filter(n => n.id !== notification.id);
        }
      });
    }
    void this.router.navigate(['/notifications']);
  }
}
