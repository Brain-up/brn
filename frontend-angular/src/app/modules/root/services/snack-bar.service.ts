import { Injectable } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';

@Injectable()
export class SnackBarService {
  private readonly DISPLAY_DURATION_IN_MS = 2000;

  constructor(private readonly matSnackBar: MatSnackBar) {}

  public showHappySnackbar(message: string): void {
    this.matSnackBar.open(message, ' 😊 ', { duration: this.DISPLAY_DURATION_IN_MS });
  }

  public showSadSnackbar(message: string): void {
    this.matSnackBar.open(message, ' 😪 ', { duration: this.DISPLAY_DURATION_IN_MS });
  }
}
