import { Injectable } from '@angular/core';
import { HttpClient, HttpParams, HttpHeaders, HttpErrorResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { GetUploadModel } from '../../model/GetUploadModel';

@Injectable()
export class UploadService {
    getUploadData(folder: string, filename: string): Observable<GetUploadModel> {
        const resolvedPath =  folder + filename;
        return this.httpClient.get<GetUploadModel>(`/api/cloud/upload?filePath=${resolvedPath}`);
    }
    sendFormData2(action: string, body: FormData): Observable<{}> | Observable<HttpErrorResponse> {
        return this.httpClient.post(action, body);
    }
    sendFormData(action: string, body: FormData): Observable<{}> {
        return this.httpClient.post(action, body);
    }
    constructor(private httpClient: HttpClient) {}
}
