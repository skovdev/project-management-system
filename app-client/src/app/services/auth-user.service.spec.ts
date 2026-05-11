import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';

import { AuthUserService } from './auth-user.service';

describe('AuthUserService', () => {
  let service: AuthUserService;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient()] });
    service = TestBed.inject(AuthUserService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
