import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../environments/environment';
import { ApiResponse } from '../models/api-response.model';
import { Page } from '../models/page.model';
import { NotificationDto } from '../models/notification.model';

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private apiUrl = environment.apiGatewayUrl + '/notifications';

  constructor(private http: HttpClient) {}

  getNotifications(page = 0, size = 20): Observable<Page<NotificationDto>> {
    return this.http.get<Page<NotificationDto>>(this.apiUrl, {
      params: { page, size }
    });
  }

  getUnread(): Observable<ApiResponse<NotificationDto[]>> {
    return this.http.get<ApiResponse<NotificationDto[]>>(`${this.apiUrl}/unread`);
  }

  markAsRead(id: string): Observable<ApiResponse<NotificationDto>> {
    return this.http.put<ApiResponse<NotificationDto>>(`${this.apiUrl}/${id}/read`, {});
  }

  deleteNotification(id: string): Observable<ApiResponse<null>> {
    return this.http.delete<ApiResponse<null>>(`${this.apiUrl}/${id}`);
  }
}
