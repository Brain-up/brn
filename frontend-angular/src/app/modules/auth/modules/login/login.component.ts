import { ChangeDetectionStrategy, Component, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Observable, Subject } from 'rxjs';
import { AuthenticationApiService } from '@auth/services/api/authentication-api.service';
import { takeUntil } from 'rxjs/operators';
import { AuthTokenService } from '@root/services/auth-token.service';
import { Router } from '@angular/router';
import { HOME_PAGE } from '@shared/constants/common-constants';

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

  constructor(
    private readonly router: Router,
    private readonly formBuilder: FormBuilder,
    private readonly authenticationApiService: AuthenticationApiService,
    private readonly authTokenService: AuthTokenService
  ) {}

  ngOnInit(): void {
    this.loginForm = this.formBuilder.group({
      grant_type: ['password'],
      username: ['', Validators.required],
      password: ['', Validators.required],
    });
  }

  ngOnDestroy(): void {
    this.destroyer$.next();
    this.destroyer$.complete();
  }

  public login(): void {
    this.authenticationApiService
      .login(this.loginForm.value)
      .pipe(takeUntil(this.destroyer$))
      .subscribe((authToken) => {
        this.authTokenService.setAuthToken(authToken);
        this.router.navigateByUrl(HOME_PAGE);
      });
  }
}
