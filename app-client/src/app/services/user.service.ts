import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../environments/environment';
import { Page } from '../models/page.model';
import { UserDto } from '../models/user.model';

@Injectable({ providedIn: 'root' })
export class UserService {
  private apiUrl = environment.apiGatewayUrl + '/users';

  constructor(private http: HttpClient) {}

  getUsers(page = 0, size = 20): Observable<Page<UserDto>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<Page<UserDto>>(this.apiUrl, { params });
  }

  getUserById(id: string): Observable<UserDto> {
    return this.http.get<UserDto>(`${this.apiUrl}/${id}`);
  }

  updateUser(id: string, user: UserDto): Observable<UserDto> {
    return this.http.put<UserDto>(`${this.apiUrl}/${id}`, user);
  }

  deleteUser(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
