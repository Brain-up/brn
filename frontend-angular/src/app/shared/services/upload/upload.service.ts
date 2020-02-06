import {Inject, Injectable, InjectionToken, Optional} from '@angular/core';
import {fromNullable, getOrElse, map} from 'fp-ts/lib/Option';
import {pipe} from 'fp-ts/lib/pipeable';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {upload} from '../utils';

export const UPLOAD_DESTINATION: InjectionToken<string> = new InjectionToken('upload.destination');

@Injectable()
export class UploadService {

  constructor(@Optional() @Inject(UPLOAD_DESTINATION) private uploadDestination: string,
              private httpClient: HttpClient) {
  }

  upload(files: Set<File>, params?: Record<string, any>): { [key: string]: { progress: Observable<number> } } {
    return pipe(
      fromNullable(this.uploadDestination),
      map(upload(files, params)(this.httpClient)),
      getOrElse(null)
    );
  }

}
