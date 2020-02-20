import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { of, Observable } from 'rxjs';
import { delay } from 'rxjs/operators';

@Injectable()
export class FolderService {
    private mockedResponse = `<ListBucketResult xmlns="http://s3.amazonaws.com/doc/2006-03-01/">
        <Name>bucketname</Name>
        <Prefix></Prefix>
        <Marker></Marker>
        <MaxKeys>1000</MaxKeys>
        <IsTruncated>false</IsTruncated>
        <Contents>
          <Key>123/</Key>
          <LastModified>2020-02-14T09:02:56.000Z</LastModified>
          <ETag>&quot;d41d8cd98f00b204e9800998ecf8427e&quot;</ETag>
          <Size>0</Size>
          <StorageClass>STANDARD</StorageClass>
        </Contents>
        <Contents>
          <Key>123/456/</Key>
          <LastModified>2020-02-14T09:03:02.000Z</LastModified>
          <ETag>&quot;d41d8cd98f00b204e9800998ecf8427e&quot;</ETag>
          <Size>0</Size>
          <StorageClass>STANDARD</StorageClass>
        </Contents>
        <Contents>
          <Key>123/test.jpeg</Key>
          <LastModified>2020-02-14T09:10:48.000Z</LastModified>
          <ETag>&quot;7ae9191463771fc777ce0e85b276b2be&quot;</ETag>
          <Size>119709</Size>
          <StorageClass>STANDARD</StorageClass>
        </Contents>
        <Contents>
          <Key>test.jpeg</Key>
          <LastModified>2020-02-14T09:02:47.000Z</LastModified>
          <ETag>&quot;7ae9191463771fc777ce0e85b276b2be&quot;</ETag>
          <Size>119709</Size>
          <StorageClass>STANDARD</StorageClass>
        </Contents>
      </ListBucketResult>`;
    getFolders(): Observable<Array<string>> {
      const domParser = new DOMParser();
      let xmlDoc = domParser.parseFromString(this.mockedResponse, 'application/xml');
      let keyEntries = xmlDoc.querySelectorAll('Contents Key');
      let folders = this.selectFolders(keyEntries).map(item=> item.textContent.replace(/^\/+|\/+$/, ''));
      return of(folders).pipe(
          delay(300)
      )
    }
    selectFolders(keys: NodeList): Array<Node> {
        let folders: Array<Node> = [];
        for(let i = 0; i < keys.length; i++) {
            let fileOrFolderName = keys[i].textContent;
            if(fileOrFolderName.indexOf('.') > 0) {
                continue;
            }else {
                folders.push(keys[i]);
            }
        }
        return folders;
    }
    constructor(private httpClient: HttpClient) {}
}