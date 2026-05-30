import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { NotificationService } from '../../../services/notification.service';
import { NotificationDto } from '../../../models/notification.model';

@Component({
  selector: 'app-notification-list',
  standalone: true,
  imports: [
    CommonModule,
    MatTableModule, MatPaginatorModule, MatButtonModule,
    MatIconModule, MatProgressSpinnerModule, MatCardModule, MatChipsModule
  ],
  templateUrl: './notification-list.component.html',
  styleUrl: './notification-list.component.css'
})
export class NotificationListComponent implements OnInit {
  displayedColumns = ['type', 'title', 'message', 'createdAt', 'status', 'actions'];
  notifications: NotificationDto[] = [];
  totalElements = 0;
  pageSize = 10;
  pageIndex = 0;
  loading = true;

  constructor(
    private notificationService: NotificationService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.loadNotifications();
  }

  loadNotifications(): void {
    this.loading = true;
    this.notificationService.getNotifications(this.pageIndex, this.pageSize).subscribe({
      next: (page) => {
        this.notifications = page.content;
        this.totalElements = page.totalElements;
        this.loading = false;
      },
      error: () => { this.loading = false; }
    });
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadNotifications();
  }

  markAsRead(notification: NotificationDto): void {
    if (notification.read) return;
    this.notificationService.markAsRead(notification.id).subscribe({
      next: () => {
        notification.read = true;
        this.snackBar.open('Notification marked as read.', 'Close', { duration: 2000 });
      },
      error: () => this.snackBar.open('Failed to mark as read.', 'Close', { duration: 3000 })
    });
  }

  deleteNotification(id: string): void {
    if (!confirm('Delete this notification?')) return;
    this.notificationService.deleteNotification(id).subscribe({
      next: () => {
        this.snackBar.open('Notification deleted.', 'Close', { duration: 2000 });
        this.loadNotifications();
      },
      error: () => this.snackBar.open('Failed to delete notification.', 'Close', { duration: 3000 })
    });
  }
}
