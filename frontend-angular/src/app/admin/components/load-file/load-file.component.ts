import {ChangeDetectionStrategy, Component, OnInit, Self} from '@angular/core';
import {UPLOAD_DESTINATION, UploadService} from '../../../shared/upload-file/service/upload.service';
import {fold, fromNullable} from 'fp-ts/lib/Option';
import {pipe} from 'fp-ts/lib/pipeable';
import {forkJoin, noop, Observable, throwError} from 'rxjs';
import {catchError, tap} from 'rxjs/operators';
import {MatSnackBar} from '@angular/material';

@Component({
  selector: 'app-load-file',
  templateUrl: './load-file.component.html',
  styleUrls: ['./load-file.component.scss'],
  providers: [
    {
      provide: UPLOAD_DESTINATION,
      useValue: '/api/files'
    },
    UploadService
  ],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class LoadFileComponent implements OnInit {

  constructor(@Self() private uploadFileService: UploadService,
              private snackbar: MatSnackBar) {
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
        tap(_ => this.snackbar.open(`${Object.keys(fileInfo).join(',')} was successfully uploaded`, ' 😊 ', {
          duration: 2000
        })),
        catchError(err => {
          this.snackbar.open(err.message, ' 😪 ', {
            duration: 2000
          });
          return throwError(err);
        })
      )
      .subscribe();
  }
}
