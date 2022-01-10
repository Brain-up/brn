import firebase from 'firebase/app';
import { AngularFireAuth } from '@angular/fire/auth';
import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { TokenService } from '@root/services/token.service';
import {
  AUTH_PAGE_URL,
  HOME_PAGE_URL,
} from '@shared/constants/common-constants';

@Injectable()
export class AuthenticationApiService {
  private authState: any = null;

  constructor(
    private readonly angularFireAuth: AngularFireAuth,
    private readonly router: Router,
    private readonly tokenService: TokenService,
  ) {
    this.initAuth();
  }

  private initAuth(): void {
    this.angularFireAuth.authState.subscribe((auth) => {
      if (auth) {
        this.authState = auth;
      } else {
        this.tokenService.removeToken();
        this.tokenService.removeToken('SELECTED_USER');
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

  public googleLogin(): unknown {
    const provider = new firebase.auth.GoogleAuthProvider();
    return this.oAuthLogin(provider)
      .then(() => {
        this.tokenService.setToken(this.authState);
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
        this.tokenService.setToken(this.authState);
        this.router.navigateByUrl(HOME_PAGE_URL);
      })
      .catch((error) => {
        console.log('error', error);
        throw error;
      });
  }

  public signOut(): void {
    this.angularFireAuth.signOut();
    this.tokenService.removeToken();
    this.tokenService.removeToken('SELECTED_USER');
    this.router.navigate([AUTH_PAGE_URL]);
  }
}
