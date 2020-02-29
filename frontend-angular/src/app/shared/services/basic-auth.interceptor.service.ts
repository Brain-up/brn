import {Injectable} from '@angular/core';
import {HttpEvent, HttpHandler, HttpInterceptor, HttpRequest} from '@angular/common/http';
import {Observable} from 'rxjs';

@Injectable({
  providedIn: 'root'
})

export class BasicAuthInterceptor implements HttpInterceptor {

  constructor() {
  }

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    let headers;
    const unAuthenticatedUrls = [
      'http://testbucket12356123456.s3.amazonaws.com',
      'https://s3.us-south.cloud-object-storage.appdomain.cloud/cloud-object-storage-gg-cos-standard-koy'
    ];
    if (unAuthenticatedUrls.indexOf(req.url) < 0) {
      headers = req.headers.set('Authorization', 'Basic YWRtaW46YWRtaW4=');
    }

    const authRequest = req.clone({
      headers
    });
    return next.handle(authRequest);
  }
}
