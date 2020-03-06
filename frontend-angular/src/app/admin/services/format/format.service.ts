import { Injectable } from "@angular/core";
import { HttpClient } from '@angular/common/http';
import { GetFormatReturnData } from '../../model/get-format-return-data';

@Injectable()
export class FormatService {
    getFormat(seriesId: number) {
        return this.httpClient.get<GetFormatReturnData>('/api/series/fileFormat/'+seriesId);
    }
    constructor(private httpClient: HttpClient) {}
}