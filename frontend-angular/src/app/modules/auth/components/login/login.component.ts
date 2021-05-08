import { ChangeDetectionStrategy, Component, OnDestroy, OnInit } from '@angular/core';
import { FormGroup, FormControl, Validators } from '@angular/forms';
import { Store } from '@ngrx/store';
import { Observable, Subject } from 'rxjs';
import { withLatestFrom, debounceTime, takeUntil } from 'rxjs/operators';
import { AppStateModel } from 'src/app/models/app-state.model';
import { DEBOUNCE_TIME_IN_MS } from 'src/app/modules/shared/constants/time-constants';
import * as fromAuthActions from '../../ngrx/actions';
import { selectAuthError } from '../../ngrx/reducers';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LoginComponent implements OnInit, OnDestroy {
  private readonly destroyer$ = new Subject<void>();

  public loginForm: FormGroup;
  public loginError: Observable<string>;

  constructor(private readonly store: Store<AppStateModel>) {}

  ngOnInit(): void {
    this.loginForm = new FormGroup({
      grant_type: new FormControl('password'),
      username: new FormControl('', Validators.required),
      password: new FormControl('', Validators.required),
    });

    this.loginError = this.store.select(selectAuthError);

    this.loginForm.valueChanges
      .pipe(debounceTime(DEBOUNCE_TIME_IN_MS), withLatestFrom(this.loginError), takeUntil(this.destroyer$))
      .subscribe(([changes, error]) => {
        if (error) {
          this.store.dispatch(fromAuthActions.clearErrorAction());
        }
      });
  }

  ngOnDestroy(): void {
    this.destroyer$.next();
    this.destroyer$.complete();
  }

  public onLogin(): void {
    this.store.dispatch(fromAuthActions.createSessionRequestAction(this.loginForm.value));
  }
}
