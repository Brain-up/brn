import { Injectable } from '@angular/core';
import { CanActivate, UrlTree, Router } from '@angular/router';
import { Store, select } from '@ngrx/store';
import { map } from 'rxjs/operators';
import { Observable } from 'rxjs';
import { AppStateModel } from 'src/app/models/app-state.model';
import { selectAuthState } from 'src/app/modules/auth/ngrx/reducers';

@Injectable()
export class AuthGuardService implements CanActivate {
  constructor(private readonly store: Store<AppStateModel>, private readonly router: Router) {}

  public canActivate(): Observable<boolean | UrlTree> {
    return this.store.pipe(
      select(selectAuthState),
      map((authState) => {
        return authState ? this.router.createUrlTree(['/']) : !authState;
      })
    );
  }
}
