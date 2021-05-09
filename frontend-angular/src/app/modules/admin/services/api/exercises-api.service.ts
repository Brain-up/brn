import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { pluck } from 'rxjs/operators';
import { Observable } from 'rxjs';
import { Exercise } from '../../models/exercise';

@Injectable()
export class ExercisesApiService {
  constructor(private readonly httpClient: HttpClient) {}

  getExercisesBySubGroupId(subGroupId: string): Observable<Exercise[]> {
    return this.httpClient.get<any>('/api/admin/exercises', {params: {subGroupId}}).pipe(
      pluck('data')
    );
  }
}
