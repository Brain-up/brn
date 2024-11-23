import { Injectable, inject } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { isObservable, Observable, of } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class SnackBarService {
  private readonly matSnackBar = inject(MatSnackBar);

  private readonly DISPLAY_DURATION_IN_MS = 2000;

  public success(message: string | Observable<string>): void {
    const message$ = isObservable(message) ? message : of(message);
    message$.subscribe((m) => this.matSnackBar.open(m, ' 😊 ', { duration: this.DISPLAY_DURATION_IN_MS }));
  }

  public error(message: string | Observable<string>): void {
    const message$ = isObservable(message) ? message : of(message);
    message$.subscribe((m) => this.matSnackBar.open(m, ' 😪 ', { duration: this.DISPLAY_DURATION_IN_MS }));
  }
}
