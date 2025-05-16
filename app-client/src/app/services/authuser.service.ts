import { Injectable } from '@angular/core';
import { HttpClient } from "@angular/common/http";

import { environment } from "../environments/environment";

import { Observable } from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class AuthUserService {

  private apiUrl = environment.apiGatewayUrl;

  constructor(private http: HttpClient) {}

  signUp(authUser: any): Observable<any> {
    return this.http.post(this.apiUrl + "/auth/sign-up", authUser, { responseType: 'text' });
  }
}
