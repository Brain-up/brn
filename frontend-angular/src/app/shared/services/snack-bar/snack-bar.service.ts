import { Injectable } from "@angular/core";
import { MatSnackBar } from '@angular/material';

@Injectable()
export class SnackBarService {
    showHappySnackbar( message: string) {
        this.snackbar.open(message, ' 😊 ', {duration: 2000});
    }

    showSadSnackbar( err: { message: string }) {
        this.snackbar.open(err.message, ' 😪 ', { duration: 2000 });
    }
    constructor(private snackbar: MatSnackBar) {}
}