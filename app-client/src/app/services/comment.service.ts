import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../environments/environment';
import { ApiResponse } from '../models/api-response.model';
import { Page } from '../models/page.model';
import { CommentDto, CommentRequestDto } from '../models/comment.model';

@Injectable({ providedIn: 'root' })
export class CommentService {
  private baseUrl = environment.apiGatewayUrl + '/tasks';

  constructor(private http: HttpClient) {}

  getComments(taskId: string, page = 0, size = 5): Observable<Page<CommentDto>> {
    const params = new HttpParams()
      .set('page', page)
      .set('size', size)
      .set('sort', 'createdAt,desc');
    return this.http.get<Page<CommentDto>>(`${this.baseUrl}/${taskId}/comments`, { params });
  }

  createComment(taskId: string, dto: CommentRequestDto): Observable<ApiResponse<CommentDto>> {
    return this.http.post<ApiResponse<CommentDto>>(`${this.baseUrl}/${taskId}/comments`, dto);
  }

  updateComment(taskId: string, commentId: string, dto: CommentRequestDto): Observable<ApiResponse<CommentDto>> {
    return this.http.put<ApiResponse<CommentDto>>(`${this.baseUrl}/${taskId}/comments/${commentId}`, dto);
  }

  deleteComment(taskId: string, commentId: string): Observable<ApiResponse<null>> {
    return this.http.delete<ApiResponse<null>>(`${this.baseUrl}/${taskId}/comments/${commentId}`);
  }
}
