import { AngularFireAuth, AngularFireAuthModule } from '@angular/fire/auth';
import { AngularFireModule } from '@angular/fire';
import { AuthenticationApiService } from './authentication-api.service';
import { of } from 'rxjs';
import { environment } from 'src/environments/environment';
import { RouterTestingModule } from '@angular/router/testing';
import { TestBed } from '@angular/core/testing';
import { TokenService } from '@root/services/token.service';

const authState = {
  displayName: null,
  email: 'Test mail',
  isAnonymous: true,
  uid: '17WvU2Vj58SnTz8v7EqyYYb0WRc2',
};

const mockAngularFireAuth: any = {
  auth: jasmine.createSpyObj('auth', ['']),
  authState: of(authState),
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
        { provide: AngularFireAuth, useValue: mockAngularFireAuth },
        {
          provide: AuthenticationApiService,
          useClass: AuthenticationApiService,
        },
        TokenService,
      ],
    });
    service = TestBed.inject(AuthenticationApiService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should authState not be set', () => {
    Reflect.set(service, 'authState', null);
    expect(service.currentUser).toEqual(null);
  });

  it('should authState be set', () => {
    expect(service.currentUser).toBe(authState);
  });

  it('should set currentUid to empty', () => {
    Reflect.set(service, 'authState', null);
    expect(service.currentUserId).toBe('');
  });

  it('should set currentUid to authState uid', () => {
    expect(service.currentUserId).toBe(authState.uid);
  });

  it('should set isUserAnonymousLoggedIn to undefined', () => {
    Reflect.set(service, 'authState', null);
    expect(service.isUserAnonymousLoggedIn).toBe(false);
  });

  it('should set isUserAnonymousLoggedIn to authState uid', () => {
    expect(service.isUserAnonymousLoggedIn).toBe(true);
  });

  it('should set currentUserName to undefined', () => {
    expect(service.currentUserName).toEqual('Test mail');
  });

  it('should set isUserEmailLoggedIn to false', () => {
    Reflect.set(service, 'authState.', null);
    expect(service.isUserEmailLoggedIn).toEqual(false);
  });
});
