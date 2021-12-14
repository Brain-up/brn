import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { TokenService } from '@root/services/token.service';
import { UserCredential } from '@root/models/auth-token';
import {
  HttpEvent,
  HttpHandler,
  HttpInterceptor,
  HttpRequest,
} from '@angular/common/http';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  constructor(private readonly tokenService: TokenService) {}

  public intercept(
    req: HttpRequest<any>,
    next: HttpHandler,
  ): Observable<HttpEvent<any>> {
    const authToken = this.tokenService.getToken<UserCredential>();

    if (authToken) {
      return next.handle(
        req.clone({
          setHeaders: {
            Authorization: `Bearer ${authToken.user.stsTokenManager.accessToken}`,
          },
        }),
      );
    }

    return next.handle(req);
  }
}
