import { Injectable } from '@angular/core';
import { HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { Observable } from 'rxjs';
import { SessionTokenService } from '../../auth/services/session/session-token.service';

@Injectable()
export class BasicAuthInterceptor implements HttpInterceptor {
  public intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    let headers;
    const unAuthenticatedUrls = [
      'http://testbucket12356123456.s3.amazonaws.com',
      'https://s3.us-south.cloud-object-storage.appdomain.cloud/cloud-object-storage-gg-cos-standard-koy',
    ];
    const tokenData = SessionTokenService.getToken();
    if (!unAuthenticatedUrls.includes(req.url) && tokenData) {
      headers = req.headers.set('Authorization', `Basic ${tokenData.access_token}`);
    }

    const authRequest = req.clone({ headers });
    return next.handle(authRequest);
  }
}
