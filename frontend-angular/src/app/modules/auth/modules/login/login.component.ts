import {
  ChangeDetectionStrategy,
  Component,
  OnDestroy,
  OnInit,
} from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Observable, Subject } from 'rxjs';
import { AuthenticationApiService } from '@auth/services/api/authentication-api.service';
import { Router } from '@angular/router';
import { SnackBarService } from '@root/services/snack-bar.service';
import { TranslateService } from '@ngx-translate/core';

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
    private readonly authenticationApiService: AuthenticationApiService,
    private readonly formBuilder: FormBuilder,
    private readonly router: Router,
    private readonly snackBarService: SnackBarService,
    private readonly translateService: TranslateService,
  ) {}

  ngOnInit(): void {
    this.loginForm = this.formBuilder.group({
      grant_type: ['password'],
      email: ['', Validators.required],
      password: ['', Validators.required],
    });
  }

  ngOnDestroy(): void {
    this.destroyer$.next();
    this.destroyer$.complete();
  }

  public login(): void {
    const { email, password } = this.loginForm.value;
    this.authenticationApiService.loginWithEmail(email, password).catch(() => {
      this.snackBarService.error(
        this.translateService.get('Auth.Modules.Login.Error'),
      );
      this.router.navigate(['/']);
    });
  }
}
