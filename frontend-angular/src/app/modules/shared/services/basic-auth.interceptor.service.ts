import {Injectable} from '@angular/core';
import {HttpEvent, HttpHandler, HttpInterceptor, HttpRequest} from '@angular/common/http';
import {Observable, of, EMPTY} from 'rxjs';
import { Store } from '@ngrx/store';
import { AppStateModel } from 'src/app/models/app-state.model';
import { map, mergeMap, withLatestFrom, tap } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})

export class BasicAuthInterceptor implements HttpInterceptor {
  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    let headers;
    const unAuthenticatedUrls = [
      'http://testbucket12356123456.s3.amazonaws.com',
      'https://s3.us-south.cloud-object-storage.appdomain.cloud/cloud-object-storage-gg-cos-standard-koy'
    ];
    if (unAuthenticatedUrls.indexOf(req.url) < 0) {
      // tslint:disable-next-line:max-line-length
      headers = req.headers.set('Authorization', 'Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbkBhZG1pbi5jb20iLCJhdXRob3JpdGllcyI6WyJST0xFX0FETUlOIl0sImlhdCI6MTU4NjA5ODc0MiwiZXhwIjoxNTg2MTg1MTQyfQ.VSdRDZcAx5-Pn0LgfBnMfUmlWgkFaEJdgxTt_OvALrIYdzNsRq6a1lCC5TeQjPeuNh54wPMwPH9YHRml1_383Q');
    }

    const authRequest = req.clone({
      headers
    });
    return next.handle(authRequest);

  }
  constructor(private store: Store<AppStateModel>) {
  }
}
