import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../environments/environment';
import { ApiResponse } from '../models/api-response.model';
import { SignInDto, SignUpDto, SignInResponse } from '../models/auth.model';

@Injectable({ providedIn: 'root' })
export class AuthUserService {
  private apiUrl = environment.apiGatewayUrl;

  constructor(private http: HttpClient) {}

  signIn(dto: SignInDto): Observable<ApiResponse<SignInResponse>> {
    return this.http.post<ApiResponse<SignInResponse>>(this.apiUrl + '/auth/sign-in', dto);
  }

  signUp(dto: SignUpDto): Observable<ApiResponse<string>> {
    return this.http.post<ApiResponse<string>>(this.apiUrl + '/auth/sign-up', dto);
  }
}
