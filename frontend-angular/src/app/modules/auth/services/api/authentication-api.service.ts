import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { LoginData } from '../../models/login-data';
import { AuthToken } from '@root/models/auth-token';
import { AngularFireAuth } from '@angular/fire/auth';
import { Router } from '@angular/router';

@Injectable()
export class AuthenticationApiService {
  private authState: any = null;

  constructor(
    private readonly angularFireAuth: AngularFireAuth,
    private readonly httpClient: HttpClient,
    private readonly router: Router,
  ) {
    this.angularFireAuth.authState.subscribe((auth) => {
      this.authState = auth;
    });
  }

  public login(data: LoginData): Observable<AuthToken> {
    return this.httpClient.post<AuthToken>('/api/brnlogin', data);
  }

  public get isUserAnonymousLoggedIn(): boolean {
    return this.authState !== null ? this.authState.isAnonymous : false;
  }

  public get currentUserId(): string {
    return this.authState !== null ? this.authState.uid : '';
  }

  public get currentUserName(): string {
    return this.authState['email'];
  }

  public get currentUser(): any {
    return this.authState !== null ? this.authState : null;
  }

  public get isUserEmailLoggedIn(): boolean {
    if (this.authState !== null && !this.isUserAnonymousLoggedIn) {
      return true;
    } else {
      return false;
    }
  }

  public signUpWithEmail(email: string, password: string) {
    return this.angularFireAuth
      .createUserWithEmailAndPassword(email, password)
      .then((user) => {
        this.authState = user;
      })
      .catch((error) => {
        console.log(error);
        throw error;
      });
  }

  public loginWithEmail(email: string, password: string) {
    return this.angularFireAuth
      .signInWithEmailAndPassword(email, password)
      .then((user) => {
        this.authState = user;
      })
      .catch((error) => {
        console.log(error);
        throw error;
      });
  }

  public signOut(): void {
    this.angularFireAuth.signOut();
    this.router.navigate(['/']);
  }
}
