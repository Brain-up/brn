import { Injectable } from '@angular/core';
import { HttpErrorResponse, HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { Observable } from 'rxjs';
import { StatusCodes } from 'http-status-codes';
import { tap } from 'rxjs/operators';
import { Router } from '@angular/router';
import { SnackBarService } from '@root/services/snack-bar.service';
import { AUTH_PAGE } from '@shared/constants/common-constants';
import { AuthTokenService } from '@root/services/auth-token.service';
import { TranslateService } from '@ngx-translate/core';

@Injectable()
export class ExceptionsInterceptor implements HttpInterceptor {
  constructor(
    private readonly router: Router,
    private readonly snackBarService: SnackBarService,
    private readonly authTokenService: AuthTokenService,
    private readonly translateService: TranslateService
  ) {}

  public intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    return next.handle(req).pipe(
      tap({
        error: (err) => {
          if (err instanceof HttpErrorResponse) {
            switch (err.status) {
              case StatusCodes.UNAUTHORIZED:
                this.snackBarService.error(this.translateService.get('Root.Interceptors.Exceptions.Unauthorized'));
                this.authTokenService.removeAuthToken();
                this.router.navigateByUrl(AUTH_PAGE);
                break;

              default:
                this.snackBarService.error(this.translateService.get('Root.Interceptors.Exceptions.UnknownError'));
                this.authTokenService.removeAuthToken();
                this.router.navigateByUrl(AUTH_PAGE);
                break;
            }
          }
        },
      })
    );
  }
}
