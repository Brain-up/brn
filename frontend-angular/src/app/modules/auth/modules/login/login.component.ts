import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, OnDestroy, OnInit, inject } from '@angular/core';
import { ReactiveFormsModule, UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthenticationApiService } from '@auth/services/api/authentication-api.service';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { SnackBarService } from '@root/services/snack-bar.service';
import { Observable, Subject } from 'rxjs';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss'],
  imports: [CommonModule, ReactiveFormsModule, TranslateModule],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class LoginComponent implements OnInit, OnDestroy {
  private readonly authenticationApiService = inject(AuthenticationApiService);
  private readonly formBuilder = inject(UntypedFormBuilder);
  private readonly router = inject(Router);
  private readonly snackBarService = inject(SnackBarService);
  private readonly translateService = inject(TranslateService);

  private readonly destroyer$ = new Subject<void>();
  public loginForm: UntypedFormGroup;
  public loginError: Observable<string>;

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
