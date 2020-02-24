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
        return this.httpClient.post(action, body);
    }
    uploadConfigToApl(formData: FormData) {
        return this.httpClient.post(`/api/loadTasksFile`, formData, {headers: {'Content-Type': 'multipart/form-data; boundary=----WebKitFormBoundaryXEZglQkzkAJigSnp'}})
    }
    constructor(private httpClient: HttpClient) {}
}