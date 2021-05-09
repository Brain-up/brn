import { Injectable } from '@angular/core';
import { HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthTokenService } from '@root/services/auth-token.service';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  private readonly URL_BLACK_LIST = new Set([
    'http://testbucket12356123456.s3.amazonaws.com',
    'https://s3.us-south.cloud-object-storage.appdomain.cloud/cloud-object-storage-gg-cos-standard-koy',
  ]);

  constructor(private readonly authTokenService: AuthTokenService) {}

  public intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const authToken = this.authTokenService.getAuthToken();

    if (!this.URL_BLACK_LIST.has(req.url) && authToken) {
      req.headers.set('Authorization', `Bearer ${authToken.access_token}`);
    }

    return next.handle(req.clone({ headers: req.headers }));
  }
}
