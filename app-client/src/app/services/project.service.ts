import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../environments/environment';
import { ApiResponse } from '../models/api-response.model';
import { Page } from '../models/page.model';
import { ProjectDto } from '../models/project.model';

@Injectable({ providedIn: 'root' })
export class ProjectService {
  private apiUrl = environment.apiGatewayUrl + '/projects';

  constructor(private http: HttpClient) {}

  getProjects(page = 0, size = 20): Observable<Page<ProjectDto>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<Page<ProjectDto>>(this.apiUrl, { params });
  }

  getProject(id: string): Observable<ApiResponse<ProjectDto>> {
    return this.http.get<ApiResponse<ProjectDto>>(`${this.apiUrl}/${id}`);
  }

  createProject(dto: ProjectDto): Observable<ApiResponse<ProjectDto>> {
    return this.http.post<ApiResponse<ProjectDto>>(this.apiUrl, dto);
  }

  updateProject(id: string, dto: ProjectDto): Observable<ApiResponse<ProjectDto>> {
    return this.http.put<ApiResponse<ProjectDto>>(`${this.apiUrl}/${id}`, dto);
  }

  deleteProject(id: string): Observable<ApiResponse<null>> {
    return this.http.delete<ApiResponse<null>>(`${this.apiUrl}/${id}`);
  }

  generateDescription(projectId: string, projectTitle: string): Observable<ApiResponse<string>> {
    const params = new HttpParams().set('projectTitle', projectTitle);
    return this.http.post<ApiResponse<string>>(
      `${this.apiUrl}/${projectId}/description`,
      null,
      { params }
    );
  }
}
