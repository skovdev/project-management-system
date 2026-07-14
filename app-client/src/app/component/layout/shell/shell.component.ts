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
import { OrganizationService } from '../../../services/organization.service';
import { CurrentOrganizationService } from '../../../services/current-organization.service';
import { NotificationDto } from '../../../models/notification.model';
import { OrganizationDto } from '../../../models/organization.model';

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

  organizations: OrganizationDto[] = [];
  currentOrgName = '';

  private sub!: Subscription;
  private notifSub!: Subscription;

  constructor(
    private authTokenService: AuthTokenService,
    private router: Router,
    private breakpointObserver: BreakpointObserver,
    private notificationService: NotificationService,
    private organizationService: OrganizationService,
    private currentOrganizationService: CurrentOrganizationService
  ) {
    this.username = authTokenService.getUsername() ?? '';
    this.initials = this.username ? this.username.charAt(0).toUpperCase() : 'U';
    this.currentOrgName = this.currentOrganizationService.getCurrentOrganizationName() ?? '';
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

    this.loadOrganizations();
  }

  loadOrganizations(): void {
    this.organizationService.getOrganizations(0, 100).subscribe({
      next: (page) => {
        this.organizations = page.content;
        if (!this.currentOrganizationService.getCurrentOrganizationId() && this.organizations.length > 0) {
          // Silent bootstrap: don't navigate away from whatever route the user landed on.
          const first = this.organizations[0];
          this.currentOrganizationService.setCurrentOrganization(first.id!, first.name);
          this.currentOrgName = first.name;
        }
      },
      error: () => { this.organizations = []; }
    });
  }

  selectOrganization(org: OrganizationDto): void {
    this.currentOrganizationService.setCurrentOrganization(org.id!, org.name);
    this.currentOrgName = org.name;
    // Projects reads the current org only on init, so send the user to a neutral
    // page after an explicit switch rather than trying to force a same-route reload.
    void this.router.navigate(['/dashboard']);
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
    this.currentOrganizationService.clear();
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
