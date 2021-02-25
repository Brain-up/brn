import { Injectable } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';


@Injectable()
export class SnackBarService {
    showHappySnackbar( message: string) {
        this.snackbar.open(message, ' 😊 ', {duration: 2000});
    }

    showSadSnackbar( message: string ) {
        this.snackbar.open(message, ' 😪 ', { duration: 2000 });
    }
    constructor(private snackbar: MatSnackBar) {}
}
