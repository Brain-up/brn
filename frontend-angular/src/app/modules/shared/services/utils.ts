import {Observable, Subject} from 'rxjs';
import {HttpClient, HttpEventType, HttpRequest, HttpResponse} from '@angular/common/http';

export const upload: (files: Set<File>, params?: Record<string, any>) => (httpClient: HttpClient) => (url: string) =>
  { [key: string]: { progress: Observable<number> } }
  = (files, params) => httpClient => url => {
  const status: { [key: string]: { progress: Observable<number> } } = {};
  files.forEach(file => {
    const formData: FormData = new FormData();
    if (params) {
      Object.entries(params).forEach(([key, value]) => formData.append(key, value));
    }
    formData.append('taskFile', file, file.name);
    const req = new HttpRequest('POST', url, formData, {
      reportProgress: true
    });
    const progress$ = new Subject<number>();
    httpClient.request(req).subscribe(event => {
        if (event.type === HttpEventType.UploadProgress) {
          const percentDone = Math.round(100 * event.loaded / event.total);
          progress$.next(percentDone);
        } else if (event instanceof HttpResponse) {
          progress$.complete();
        }
      }, err => progress$.error(err)
    );
    status[file.name] = {
      progress: progress$.asObservable()
    };
  });
  return status;
};
