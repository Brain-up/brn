import { LoginData } from './../../models/login-data';
import { TestBed } from '@angular/core/testing';
import { AuthenticationApiService } from './authentication-api.service';
import { AuthTokenService } from '@root/services/auth-token.service';
import { RouterTestingModule } from '@angular/router/testing';
import { AngularFireModule } from '@angular/fire';
import { AngularFireAuthModule } from '@angular/fire/auth';
import { environment } from 'src/environments/environment';

const data: LoginData = {
  grant_type: 'password',
  password: 'admin',
  email: 'admin@admin.com',
};

describe('AuthenticationApiService', () => {
  let service: AuthenticationApiService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        AngularFireModule.initializeApp(environment.firebaseConfig),
        AngularFireAuthModule,
        RouterTestingModule,
      ],
      providers: [
        AuthenticationApiService,
        AuthTokenService,
      ],
    });
    service = TestBed.inject(AuthenticationApiService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
