import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { of, Observable } from 'rxjs';
import { delay, tap, mergeMap } from 'rxjs/operators';
import { map } from 'fp-ts/lib/ReadonlyRecord';
import {pluck} from 'rxjs/operators';

@Injectable()
export class FolderService {

    getFolders(): Observable<Array<string>> {
        return this.httpClient.get<any>(`/api/cloud/folders`).pipe(
          pluck('data')
        );
    }
    selectFolders(keys: NodeList): Array<Node> {
        const folders: Array<Node> = [];
        const arrKeys = Array.from(keys);
        for (const key of arrKeys) {
            const fileOrFolderName = key.textContent;
            if (fileOrFolderName.indexOf('.') > 0) {
                continue;
            } else {
                folders.push(key);
            }
        }
        return folders;
    }
    constructor(private httpClient: HttpClient) {}
}
