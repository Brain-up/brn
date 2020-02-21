import { Injectable } from '@angular/core';
import { HttpClient, HttpParams, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { GetUploadModel } from '../../model/GetUploadModel';

@Injectable()
export class UploadService {
    getUploadData(folder: string, filename: string) : Observable<GetUploadModel>{
        const resolvedPath =  folder + '/' + filename;
        return this.httpClient.get<GetUploadModel>(`/api/cloud/upload?filePath=${resolvedPath}`)
    }
    sendFormData(action: string, body: FormData): Observable<{}> {
        console.log(body)
        const headers = new HttpHeaders().set('Content-Type', 'multipart/form-data;boundary=WebKitFormBoundaryesm9nAfJ5PWhkAnH0000001111');
        return this.httpClient.post(action, body, {headers: headers})
    }

    constructor(private httpClient: HttpClient) {}
}