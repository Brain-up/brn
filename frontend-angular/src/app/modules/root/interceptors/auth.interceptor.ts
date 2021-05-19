import { Injectable } from '@angular/core';
import { HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthTokenService } from '@root/services/auth-token.service';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  constructor(private readonly authTokenService: AuthTokenService) {}

  public intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const authToken = this.authTokenService.getAuthToken();

    if (authToken) {
      return next.handle(req.clone({ setHeaders: { Authorization: `Bearer ${authToken.access_token}` } }));
    }

    return next.handle(req);
  }
}
