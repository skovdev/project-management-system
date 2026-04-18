import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../environments/environment';
import { ApiResponse } from '../models/api-response.model';
import { Page } from '../models/page.model';
import { TaskDto } from '../models/task.model';

@Injectable({ providedIn: 'root' })
export class TaskService {
  private apiUrl = environment.apiGatewayUrl + '/tasks';

  constructor(private http: HttpClient) {}

  getTasks(page = 0, size = 20): Observable<Page<TaskDto>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<Page<TaskDto>>(this.apiUrl, { params });
  }

  getTask(id: string): Observable<ApiResponse<TaskDto>> {
    return this.http.get<ApiResponse<TaskDto>>(`${this.apiUrl}/${id}`);
  }

  createTask(dto: TaskDto): Observable<ApiResponse<TaskDto>> {
    return this.http.post<ApiResponse<TaskDto>>(this.apiUrl, dto);
  }

  updateTask(id: string, dto: TaskDto): Observable<ApiResponse<TaskDto>> {
    return this.http.put<ApiResponse<TaskDto>>(`${this.apiUrl}/${id}`, dto);
  }

  deleteTask(id: string): Observable<ApiResponse<null>> {
    return this.http.delete<ApiResponse<null>>(`${this.apiUrl}/${id}`);
  }
}
