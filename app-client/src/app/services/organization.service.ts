import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../environments/environment';
import { ApiResponse } from '../models/api-response.model';
import { Page } from '../models/page.model';
import { OrganizationDto, OrganizationMemberDto, OrganizationRole } from '../models/organization.model';

@Injectable({ providedIn: 'root' })
export class OrganizationService {
  private apiUrl = environment.apiGatewayUrl + '/organizations';

  constructor(private http: HttpClient) {}

  getOrganizations(page = 0, size = 20): Observable<Page<OrganizationDto>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<Page<OrganizationDto>>(this.apiUrl, { params });
  }

  getOrganization(id: string): Observable<ApiResponse<OrganizationDto>> {
    return this.http.get<ApiResponse<OrganizationDto>>(`${this.apiUrl}/${id}`);
  }

  createOrganization(dto: OrganizationDto): Observable<ApiResponse<OrganizationDto>> {
    return this.http.post<ApiResponse<OrganizationDto>>(this.apiUrl, dto);
  }

  updateOrganization(id: string, dto: OrganizationDto): Observable<ApiResponse<OrganizationDto>> {
    return this.http.put<ApiResponse<OrganizationDto>>(`${this.apiUrl}/${id}`, dto);
  }

  deleteOrganization(id: string): Observable<ApiResponse<null>> {
    return this.http.delete<ApiResponse<null>>(`${this.apiUrl}/${id}`);
  }

  getMembers(organizationId: string, page = 0, size = 20): Observable<Page<OrganizationMemberDto>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<Page<OrganizationMemberDto>>(`${this.apiUrl}/${organizationId}/members`, { params });
  }

  addMember(organizationId: string, userId: string, role: OrganizationRole): Observable<ApiResponse<OrganizationMemberDto>> {
    return this.http.post<ApiResponse<OrganizationMemberDto>>(
      `${this.apiUrl}/${organizationId}/members`,
      { userId, role }
    );
  }

  updateMemberRole(organizationId: string, memberId: string, role: OrganizationRole): Observable<ApiResponse<OrganizationMemberDto>> {
    return this.http.put<ApiResponse<OrganizationMemberDto>>(
      `${this.apiUrl}/${organizationId}/members/${memberId}/role`,
      { role }
    );
  }

  removeMember(organizationId: string, memberId: string): Observable<ApiResponse<null>> {
    return this.http.delete<ApiResponse<null>>(`${this.apiUrl}/${organizationId}/members/${memberId}`);
  }
}
