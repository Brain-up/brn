import { Injectable } from '@angular/core';
import { HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable()
export class StripUndefinedParamsInterceptor implements HttpInterceptor {
  public intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    let params = req.params;

    for (const key of req.params.keys()) {
      if (params.get(key) === undefined) {
        params = params.delete(key);
      }
    }

    return next.handle(req.clone({ params }));
  }
}
