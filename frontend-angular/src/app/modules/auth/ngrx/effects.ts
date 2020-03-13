import { Injectable } from "@angular/core";
import { Actions, createEffect, ofType } from '@ngrx/effects';
import * as fromAuthActions from './actions'
import { map, mergeMap, catchError } from 'rxjs/operators';
import { AuthStateModel } from '../models/auth-state.model';
import { SessionService } from '../services/session/session.service';
import { LoginSuccessModel } from '../models/login-success.model';
import { LoginFailureModel } from '../models/login-failure.model';
import { of } from 'rxjs';
@Injectable()
export class AuthEffects {
    checkForValidity(authState: AuthStateModel) : Boolean{
        return true;
    }
    checkAuthState$ = createEffect(()=> this.actions$.pipe(
        ofType(fromAuthActions.checkAuthStatusAction),
        map((action)=>{
            const authStateStr = localStorage.getItem('brnAuthState')
            // Validate
            if(!authStateStr) {
                return fromAuthActions.setAuthStatusAction({isAuthenticated: false});
            }
            const authStateObj = JSON.parse(authStateStr);
            if(this.checkForValidity(authStateObj)) {
                return fromAuthActions.setAuthStatusAction({isAuthenticated: true});
            }
            
        })
    ));
    createSession$ = createEffect(()=> this.actions$.pipe(
        ofType(fromAuthActions.createSessionRequestAction),
        mergeMap((action)=> this.sessionService.createSession(action).pipe(
            map((sessionToken: LoginSuccessModel)=> fromAuthActions.createSessionSuccessAction(sessionToken)),
            catchError((error: LoginFailureModel)=> of(fromAuthActions.createSessionFailureAction(error)))
        ))
    ))
    constructor(
        private actions$: Actions,
        private sessionService: SessionService
    ) {}
}