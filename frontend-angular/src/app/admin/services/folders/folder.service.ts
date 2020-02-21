import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { of, Observable } from 'rxjs';
import { delay, tap, mergeMap } from 'rxjs/operators';
import { map } from 'fp-ts/lib/ReadonlyRecord';

@Injectable()
export class FolderService {
    private mockedResponse = `<ListBucketResult xmlns="http://s3.amazonaws.com/doc/2006-03-01/">
    <Name>brain-up</Name>
    <Prefix/>
    <Marker/>
    <MaxKeys>1000</MaxKeys>
    <IsTruncated>false</IsTruncated>
    <Contents>
    <Key>audio/</Key>
    <LastModified>2020-02-17T13:17:22.000Z</LastModified>
    <ETag>"d41d8cd98f00b204e9800998ecf8427e"</ETag>
    <Size>0</Size>
    <StorageClass>STANDARD</StorageClass>
    </Contents>
    <Contents>
    <Key>pictures/</Key>
    <LastModified>2020-02-14T14:04:13.000Z</LastModified>
    <ETag>"d41d8cd98f00b204e9800998ecf8427e"</ETag>
    <Size>0</Size>
    <StorageClass>STANDARD</StorageClass>
    </Contents>
    <Contents>
    <Key>pictures/withWords/</Key>
    <LastModified>2020-02-17T13:17:43.000Z</LastModified>
    <ETag>"d41d8cd98f00b204e9800998ecf8427e"</ETag>
    <Size>0</Size>
    <StorageClass>STANDARD</StorageClass>
    </Contents>
    <Contents>
    <Key>tasks/</Key>
    <LastModified>2020-02-14T13:59:05.000Z</LastModified>
    <ETag>"d41d8cd98f00b204e9800998ecf8427e"</ETag>
    <Size>0</Size>
    <StorageClass>STANDARD</StorageClass>
    </Contents>
    <Contents>
    <Key>tasks/4322.jpg</Key>
    <LastModified>2020-02-17T08:42:02.000Z</LastModified>
    <ETag>"a3dabaa0edc47a158ae5a1dcb7a58cdf"</ETag>
    <Size>314726</Size>
    <StorageClass>STANDARD</StorageClass>
    </Contents>
    <Contents>
    <Key>tasks/done.jpg</Key>
    <LastModified>2020-02-17T09:21:29.000Z</LastModified>
    <ETag>"cd13cc8216510b63cccbb08b42dadf7d"</ETag>
    <Size>40454</Size>
    <StorageClass>STANDARD</StorageClass>
    </Contents>
    <Contents>
    <Key>tasks/test2.png</Key>
    <LastModified>2020-02-17T08:36:08.000Z</LastModified>
    <ETag>"5a423a20c70c85a906c30217729c2f64"</ETag>
    <Size>200829</Size>
    <StorageClass>STANDARD</StorageClass>
    </Contents>
    </ListBucketResult>`;
    
    getFolders(): Observable<Array<string>> {
        // return this.httpClient.get('https://s3.us-south.cloud-object-storage.appdomain.cloud/cloud-object-storage-gg-cos-standard-koy', {responseType: 'text'}).pipe(
        //     mergeMap((response) => {
        //         const domParser = new DOMParser();
        //         let xmlDoc = domParser.parseFromString(response, 'application/xml');

        //         let keyEntries = xmlDoc.querySelectorAll('Contents Key');
        //         console.log(keyEntries)
        //         let folders = this.selectFolders(keyEntries).map(item => item.textContent.replace(/^\/+|\/+$/, ''));
               

        //         return of(folders);
        //     })
        //     // map(response => {

        //     // })
        // )
        return of(['tasks'])
        // const domParser = new DOMParser();
        // let xmlDoc = domParser.parseFromString(this.mockedResponse, 'application/xml');
        // let keyEntries = xmlDoc.querySelectorAll('Contents Key');
        // let folders = this.selectFolders(keyEntries).map(item => item.textContent.replace(/^\/+|\/+$/, ''));
        // return of(folders).pipe(
        //     delay(300)
        // )
    }
    selectFolders(keys: NodeList): Array<Node> {
        let folders: Array<Node> = [];
        for (let i = 0; i < keys.length; i++) {
            let fileOrFolderName = keys[i].textContent;
            if (fileOrFolderName.indexOf('.') > 0) {
                continue;
            } else {
                folders.push(keys[i]);
            }
        }
        return folders;
    }
    constructor(private httpClient: HttpClient) { }
}