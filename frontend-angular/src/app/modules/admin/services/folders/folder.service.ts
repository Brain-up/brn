import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { pluck } from 'rxjs/operators';

@Injectable()
export class FolderService {
  constructor(private httpClient: HttpClient) {
  }

  getFolders(): Observable<Array<string>> {
    return this.httpClient.get<Array<string>>(`/api/cloud/folders`).pipe(
      pluck('data')
    );
  }
}
