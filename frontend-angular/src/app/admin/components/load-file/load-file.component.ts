import {ChangeDetectionStrategy, Component, OnInit, Self} from '@angular/core';
import {UPLOAD_DESTINATION, UploadService} from '../../../shared/services/upload/upload.service';
import {fold, fromNullable} from 'fp-ts/lib/Option';
import {pipe} from 'fp-ts/lib/pipeable';
import {EMPTY, forkJoin, noop, Observable, of} from 'rxjs';
import {catchError, tap} from 'rxjs/operators';
import {MatSnackBar} from '@angular/material';
import {showHappySnackbar, showSadSnackbar} from '../../../shared/pure';
import { Store } from '@ngrx/store';
import { uploadFile } from '../../../shared/ngrx/actions'
import { SnackBarService } from 'src/app/shared/services/snack-bar/snack-bar.service';

@Component({
  selector: 'app-load-file',
  templateUrl: './load-file.component.html',
  styleUrls: ['./load-file.component.scss'],
  providers: [
    {
      provide: UPLOAD_DESTINATION,
      useValue: '/api/loadTasksFile?seriesId=1'
    },
    UploadService
  ],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class LoadFileComponent implements OnInit {

  constructor(
    private store: Store<any>,
     @Self() private uploadFileService: UploadService,
     private snackBarService: SnackBarService
    ) {
  }

  ngOnInit() {
  }

  onFilesAdded(files: Set<File>) {
    pipe(
      fromNullable(this.uploadFileService.upload(files)),
      fold(noop, (fileInfo) => this.processUploadResults(fileInfo))
    );
  }

  private processUploadResults(fileInfo: { [key: string]: { progress: Observable<number> } }) {
    forkJoin(Object.values(fileInfo).map(({progress}) => progress))
      .pipe(
        tap(()=> this.snackBarService.showHappySnackbar(`${Object.keys(fileInfo).join(',')} was successfully uploaded`)),
        catchError(err => {
          this.snackBarService.showSadSnackbar(err);
          return EMPTY;
        })
      )
      .subscribe();
  }
}
