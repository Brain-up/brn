import { Injectable } from '@angular/core';
import { HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { Store } from '@ngrx/store';
import { Observable } from 'rxjs';
import { AppStateModel } from 'src/app/models/app-state.model';

@Injectable({
  providedIn: 'root'
})
export class BasicAuthInterceptor implements HttpInterceptor {
  constructor(private store: Store<AppStateModel>) {
  }

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    let headers;
    const unAuthenticatedUrls = [
      'http://testbucket12356123456.s3.amazonaws.com',
      'https://s3.us-south.cloud-object-storage.appdomain.cloud/cloud-object-storage-gg-cos-standard-koy'
    ];
    if (unAuthenticatedUrls.indexOf(req.url) < 0) {
      headers = req.headers.set('Authorization', 'Basic YWRtaW5AYWRtaW4uY29tOmFkbWlu');
    }

    const authRequest = req.clone({ headers });
    return next.handle(authRequest);
  }
}
