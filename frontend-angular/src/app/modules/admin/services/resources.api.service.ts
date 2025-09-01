import { Observable } from "rxjs";
import { GetResources } from '@admin/models/endpoints.model';
import { map, pluck } from 'rxjs/operators';
import { inject } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { Resources, UploadResourcesImage } from "@admin/models/resources.model";

export class ResourcesApiService {

    
      private httpClient = inject(HttpClient);
    
    
      public getResources(): Observable<Resources[]> {
        return this.httpClient
          .get<GetResources>('/api/resources')
          .pipe(
            pluck('data'),
            map((resourcesList: Resources[]) => resourcesList),
          );
      }
    
      public createResources(
        payload: Resources
      ): Observable<Resources> {
        return this.httpClient.post<Resources>(
          '/api/resources',
          payload
        );
      }
    
      public updateResources(
        resourceId,
        payload: Resources
      ): Observable<Resources> {
        return this.httpClient.patch<Resources>(
          `/api/resources/${resourceId}`,
          payload
        );
      }
    
      public uploadResourcesImage(
        payload: FormData
      ): Observable<UploadResourcesImage> {
        return this.httpClient.post<UploadResourcesImage>(
          '/api/cloud/upload/resources/picture',
          payload
        );
      }
}
