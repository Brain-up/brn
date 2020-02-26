import { Injectable } from "@angular/core";
import { HttpClient } from '@angular/common/http';

@Injectable()
export class FormatService {
    getFormat(seriesId: number) {
        return this.httpClient.get('/api/fileFormat/'+seriesId);
    }
    constructor(private httpClient: HttpClient) {}
}