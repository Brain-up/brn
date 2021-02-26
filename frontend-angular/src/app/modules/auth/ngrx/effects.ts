import { Injectable } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';

import { Actions, createEffect, ofType } from '@ngrx/effects';
import { map, mergeMap, catchError, switchMap, tap } from 'rxjs/operators';
import { of } from 'rxjs';

import * as fromAuthActions from './actions';
import { SessionService } from '../services/session/session.service';
import { LoginSuccessModel } from '../models/login-success.model';
import { LoginFailureModel } from '../models/login-failure.model';
import { SessionTokenService } from '../services/session/session-token.service';

@Injectable()
export class AuthEffects {
  constructor(
    private actions$: Actions,
    private sessionService: SessionService,
    private router: Router
  ) {
  }

  checkAuthState$ = createEffect(() => this.actions$.pipe(
    ofType(fromAuthActions.checkAuthStatusAction),
    map((action) => {
      const tokenData = SessionTokenService.getToken();
      if (!tokenData) {
        return fromAuthActions.setAuthStatusAction({isAuthenticated: false});
      }

      if (SessionTokenService.isValidToken(tokenData)) {
        return fromAuthActions.setAuthStatusAction({isAuthenticated: true});
      }
    })
  ));

  createSession$ = createEffect(() => this.actions$.pipe(
    ofType(fromAuthActions.createSessionRequestAction),
    mergeMap((action) => this.sessionService.createSession(action).pipe(
      switchMap((tokenData: LoginSuccessModel) => [
        fromAuthActions.createSessionSuccessAction({tokenData}),
        fromAuthActions.setAuthStatusAction({isAuthenticated: true}),
        fromAuthActions.redirectAction({location: '/admin/home'})
      ]),
      catchError((error: HttpErrorResponse) => of(
        fromAuthActions.createSessionFailureAction({
          errorObj: (error.error as LoginFailureModel),
          statusCode: error.status
        })
      )),
    ))
  ));

  sessionSuccess$ = createEffect(() => this.actions$.pipe(
    ofType(fromAuthActions.createSessionSuccessAction),
    tap(action => {
      SessionTokenService.saveToken(action.tokenData);
    })
  ), {dispatch: false});

  redirectToMainPage$ = createEffect(() => this.actions$.pipe(
    ofType(fromAuthActions.redirectAction),
    tap(action => this.router.navigateByUrl(action.location))
  ), {dispatch: false});

  destroySessionRequest$ = createEffect(() => this.actions$.pipe(
    ofType(fromAuthActions.destroySessionRequestAction),
    mergeMap(action => this.sessionService.destroySession().pipe(
      tap(_ => {
        SessionTokenService.removeToken();
      }),
      mergeMap(_ => [
        fromAuthActions.destroySessionSuccessAction(),
        fromAuthActions.redirectAction({location: '/auth/login'})
      ])
    ))
  ));
}
