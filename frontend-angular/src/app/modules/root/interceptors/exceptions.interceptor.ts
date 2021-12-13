import { AUTH_PAGE_URL } from '@shared/constants/common-constants';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Router } from '@angular/router';
import { SnackBarService } from '@root/services/snack-bar.service';
import { StatusCodes } from 'http-status-codes';
import { tap } from 'rxjs/operators';
import { TokenService } from '@root/services/token.service';
import { TranslateService } from '@ngx-translate/core';
import {
  HttpErrorResponse,
  HttpEvent,
  HttpHandler,
  HttpInterceptor,
  HttpRequest,
} from '@angular/common/http';

@Injectable()
export class ExceptionsInterceptor implements HttpInterceptor {
  constructor(
    private readonly router: Router,
    private readonly snackBarService: SnackBarService,
    private readonly tokenService: TokenService,
    private readonly translateService: TranslateService,
  ) {}

  public intercept(
    req: HttpRequest<any>,
    next: HttpHandler,
  ): Observable<HttpEvent<any>> {
    return next.handle(req).pipe(
      tap({
        error: (err) => {
          if (err instanceof HttpErrorResponse) {
            switch (err.status) {
              case StatusCodes.UNAUTHORIZED:
                this.snackBarService.error(
                  this.translateService.get(
                    'Root.Interceptors.Exceptions.Unauthorized',
                  ),
                );
                this.tokenService.removeToken();
                this.tokenService.removeToken('SELECTED_USER');
                this.router.navigateByUrl(AUTH_PAGE_URL);
                break;

              default:
                this.snackBarService.error(
                  this.translateService.get(
                    'Root.Interceptors.Exceptions.UnknownError',
                  ),
                );
                this.tokenService.removeToken();
                this.tokenService.removeToken('SELECTED_USER');
                this.router.navigateByUrl(AUTH_PAGE_URL);
                break;
            }
          }
        },
      }),
    );
  }
}
