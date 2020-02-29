import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { of, Observable } from 'rxjs';
import { delay, tap, mergeMap } from 'rxjs/operators';
import { map } from 'fp-ts/lib/ReadonlyRecord';
import {pluck} from 'rxjs/operators';

@Injectable()
export class FolderService {

    getFolders(): Observable<Array<string>> {
        return this.httpClient.get<Array<string>>(`/api/cloud/folders`).pipe(
          pluck('data')
        );
    }
    constructor(private httpClient: HttpClient) {}
}
