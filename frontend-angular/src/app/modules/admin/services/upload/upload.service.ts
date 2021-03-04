import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { GetUploadModel } from '../../model/get-upload.model';

@Injectable()
export class UploadService {
  constructor(private httpClient: HttpClient) {
  }

  getUploadData(folder: string, filename: string): Observable<GetUploadModel> {
    const resolvedPath = folder + filename;
    return this.httpClient.get<GetUploadModel>(`/api/cloud/upload?filePath=${resolvedPath}`);
  }

  sendFormData(action: string, body: FormData): Observable<{} | HttpErrorResponse> {
    return this.httpClient.post(action, body);
  }
}
