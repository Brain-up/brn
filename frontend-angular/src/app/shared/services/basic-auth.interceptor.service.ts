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
    const authRequest = req.clone({
      headers: req.headers.set('Authorization', 'Basic YWRtaW46YWRtaW4=')
    });
    return next.handle(authRequest);
  }
}
