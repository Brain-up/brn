import { Injectable } from '@angular/core';
import { AngularFireAuth } from '@angular/fire/auth';
import { Router } from '@angular/router';
import firebase from 'firebase/app';
import {
  AUTH_PAGE_URL,
  HOME_PAGE_URL,
} from '@shared/constants/common-constants';
import { AuthTokenService } from '@root/services/auth-token.service';

@Injectable()
export class AuthenticationApiService {
  private authState: any = null;

  constructor(
    private readonly angularFireAuth: AngularFireAuth,
    private readonly authTokenService: AuthTokenService,
    private readonly router: Router,
  ) {
    this.initAuth();
  }

  private initAuth(): void {
    this.angularFireAuth.authState.subscribe((auth) => {
      if (auth) {
        this.authState = auth;
      } else {
        this.authTokenService.removeAuthToken();
      }
    });
  }

  public get isUserAnonymousLoggedIn(): boolean {
    return this.authState !== null ? this.authState.isAnonymous : false;
  }

  public get currentUserId(): string {
    return this.authState !== null ? this.authState.uid : '';
  }

  public get currentUserName(): string {
    return this.authState.email;
  }

  public get currentUser(): unknown {
    return this.authState !== null ? this.authState : null;
  }

  public get isUserEmailLoggedIn(): boolean {
    if (this.authState !== null && !this.isUserAnonymousLoggedIn) {
      return true;
    } else {
      return false;
    }
  }

  public googleLogin(): any {
    const provider = new firebase.auth.GoogleAuthProvider();
    return this.oAuthLogin(provider)
      .then(() => {
        this.authTokenService.setAuthToken(this.authState);
        this.router.navigateByUrl(HOME_PAGE_URL);
      })
      .catch((error) => {
        console.log('error', error);
        throw error;
      });
  }

  private oAuthLogin(provider): any {
    return this.angularFireAuth.signInWithPopup(provider);
  }

  public loginWithEmail(email: string, password: string): any {
    return this.angularFireAuth
      .signInWithEmailAndPassword(email, password)
      .then((user) => {
        this.authState = user;
        this.authTokenService.setAuthToken(this.authState);
        this.router.navigateByUrl(HOME_PAGE_URL);
      })
      .catch((error) => {
        console.log('error', error);
        throw error;
      });
  }

  public signOut(): void {
    this.angularFireAuth.signOut();
    this.authTokenService.removeAuthToken();
    this.router.navigate([AUTH_PAGE_URL]);
  }
}
