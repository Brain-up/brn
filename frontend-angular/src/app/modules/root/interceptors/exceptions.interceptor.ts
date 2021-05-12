import { Injectable } from '@angular/core';
import { HttpErrorResponse, HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { Observable } from 'rxjs';
import { StatusCodes } from 'http-status-codes';
import { tap } from 'rxjs/operators';
import { Router } from '@angular/router';
import { SnackBarService } from '@root/services/snack-bar.service';
import { AUTH_PAGE } from '@shared/constants/common-constants';
import { AuthTokenService } from '@root/services/auth-token.service';

@Injectable()
export class ExceptionsInterceptor implements HttpInterceptor {
  constructor(
    private readonly router: Router,
    private readonly snackBarService: SnackBarService,
    private readonly authTokenService: AuthTokenService
  ) {}

  public intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    return next.handle(req).pipe(
      tap({
        error: (err) => {
          if (err instanceof HttpErrorResponse) {
            switch (err.status) {
              case 0:
              case StatusCodes.UNAUTHORIZED:
                this.snackBarService.showSadSnackbar('Unauthorized');
                this.authTokenService.removeAuthToken();
                this.router.navigateByUrl(AUTH_PAGE);
                break;

              default:
                break;
            }
          }
        },
      })
    );
  }
}
