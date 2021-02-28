import { Component, OnInit } from '@angular/core';
import { FormGroup, FormControl, Validators } from '@angular/forms';

import { Store } from '@ngrx/store';
import { Observable, Subject } from 'rxjs';
import { withLatestFrom, tap, debounceTime, takeUntil } from 'rxjs/operators';

import { AppStateModel } from 'src/app/models/app-state.model';
import * as fromAuthActions from '../../ngrx/actions';
import { selectAuthError } from '../../ngrx/reducers';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent implements OnInit {
  loginForm: FormGroup;
  loginError: Observable<string>;
  ngUnsubscribe = new Subject<void>();

  constructor(private store: Store<AppStateModel>) {
  }

  ngOnInit(): void {
    this.loginForm = new FormGroup({
      grant_type: new FormControl('password'),
      username: new FormControl('', Validators.required),
      password: new FormControl('', Validators.required)
    });

    this.loginError = this.store.select(selectAuthError);

    this.loginForm.valueChanges.pipe(
      debounceTime(300),
      withLatestFrom(this.loginError),
      tap(([changes, error]) => {
        if (error) {
          this.store.dispatch(fromAuthActions.clearErrorAction());
        }
      }),
      takeUntil(this.ngUnsubscribe)
    ).subscribe();
  }

  onLogin() {
    this.store.dispatch(fromAuthActions.createSessionRequestAction(this.loginForm.value));
  }

  ngOnDestroy(): void {
    this.ngUnsubscribe.next();
    this.ngUnsubscribe.complete();
  }
}
