import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { pluck } from 'rxjs/operators';
import { Observable } from 'rxjs';
import { UploadForm } from '../../models/upload-form';

@Injectable()
export class CloudApiService {
  constructor(private readonly httpClient: HttpClient) {}

  public getUploadForm(filePath: string): Observable<UploadForm> {
    return this.httpClient.get<{ data: UploadForm }>(`/api/cloud/upload?filePath=${filePath}`).pipe(pluck('data'));
  }

  public getFolders(): Observable<string[]> {
    return this.httpClient.get<{ data: string[] }>('/api/cloud/folders').pipe(pluck('data'));
  }
}
