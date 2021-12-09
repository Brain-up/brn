import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class DataShareService<T> {
  private data$: BehaviorSubject<T> = new BehaviorSubject<T>(undefined);

  public addData(newData: T) {
    this.data$.next(newData);
  }
  
  public getData() {
    return this.data$.asObservable();
  }

  public removeData(existingData: T) {
    this.data$.next(existingData);
  }
}
