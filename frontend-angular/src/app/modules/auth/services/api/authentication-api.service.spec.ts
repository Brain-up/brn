import { AngularFireAuthModule } from '@angular/fire/auth';
import { AngularFireModule } from '@angular/fire';
import { AuthenticationApiService } from './authentication-api.service';
import { environment } from 'src/environments/environment';
import { LoginData } from './../../models/login-data';
import { RouterTestingModule } from '@angular/router/testing';
import { TestBed } from '@angular/core/testing';
import { TokenService } from '@root/services/token.service';

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
      providers: [AuthenticationApiService, TokenService],
    });
    service = TestBed.inject(AuthenticationApiService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
