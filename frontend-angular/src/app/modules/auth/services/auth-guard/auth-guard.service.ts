import { Injectable } from '@angular/core';
import { CanActivate, RouterStateSnapshot, ActivatedRouteSnapshot, UrlTree, Router } from '@angular/router';
import { Store, select } from '@ngrx/store';
import { AppStateModel } from 'src/app/models/app-state.model';
import { selectAuthState } from 'src/app/modules/auth/ngrx/reducers';
import { map, tap } from 'rxjs/operators';
import { Observable } from 'rxjs';

@Injectable()
export class AuthGuardService implements CanActivate {
    canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<boolean | UrlTree> {
        return this.store.pipe(
            select(selectAuthState),
            map(authState => {
                return authState ? this.router.createUrlTree(['/admin']) : !authState;
            })
        );
    }
    constructor(
        private store: Store<AppStateModel>,
        private router: Router
    ) {}
}
