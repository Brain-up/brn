import { Injectable } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';

import { Actions, createEffect, ofType } from '@ngrx/effects';
import { map, mergeMap, catchError, switchMap, tap } from 'rxjs/operators';
import { of } from 'rxjs';

import * as fromAuthActions from './actions';
import { AuthStateModel } from '../models/auth-state.model';
import { SessionService } from '../services/session/session.service';
import { LoginSuccessModel } from '../models/login-success.model';
import { LoginFailureModel } from '../models/login-failure.model';

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
      const authStateStr = localStorage.getItem('brnAuthState');
      if (!authStateStr) {
        return fromAuthActions.setAuthStatusAction({isAuthenticated: false});
      }
      const authStateObj = JSON.parse(authStateStr);
      if (this.checkForValidity(authStateObj)) {
        return fromAuthActions.setAuthStatusAction({isAuthenticated: true});
      }
    })
  ));

  createSession$ = createEffect(() => this.actions$.pipe(
    ofType(fromAuthActions.createSessionRequestAction),
    mergeMap((action) => this.sessionService.createSession(action).pipe(
      switchMap((successAction: LoginSuccessModel) => {
        const sessionToken = {...action};
        delete sessionToken.type;
        const sessionTokenStr = JSON.stringify(sessionToken);
        localStorage.setItem('brnAuthState', sessionTokenStr);

        return [
          fromAuthActions.setAuthStatusAction({isAuthenticated: true}),
          fromAuthActions.createSessionSuccessAction(successAction),
          fromAuthActions.redirectAction({location: '/admin/home'})
        ];
      }),
      catchError((error: HttpErrorResponse) => of(
        fromAuthActions.createSessionFailureAction({
          errorObj: (error.error as LoginFailureModel),
          statusCode: error.status
        })
      )),
    ))
  ));

  redirectToMainPage$ = createEffect(() => this.actions$.pipe(
    ofType(fromAuthActions.redirectAction),
    tap(action => this.router.navigateByUrl(action.location))
  ), {dispatch: false});

  destroySessionRequest$ = createEffect(() => this.actions$.pipe(
    ofType(fromAuthActions.destroySessionRequestAction),
    mergeMap(action => this.sessionService.destroySession().pipe(
      tap(_ => {
        localStorage.removeItem('brnAuthState');
      }),
      mergeMap(_ => {
        {
          return [
            fromAuthActions.destroySessionSuccessAction(),
            fromAuthActions.redirectAction({location: '/auth/login'})
          ];
        }
      })
    ))
  ));

  checkForValidity(authState: AuthStateModel): boolean {
    // TODO: Check for expiration of the token
    return true;
  }
}
