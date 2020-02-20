import {ChangeDetectionStrategy, Component, OnInit, Self} from '@angular/core';
import {UPLOAD_DESTINATION, UploadService} from '../../../shared/services/upload/upload.service';
import {fold, fromNullable} from 'fp-ts/lib/Option';
import {pipe} from 'fp-ts/lib/pipeable';
import {EMPTY, forkJoin, noop, Observable, of} from 'rxjs';
import {catchError, tap} from 'rxjs/operators';
import { SnackBarService } from 'src/app/shared/services/snack-bar/snack-bar.service';
import { FormGroup, FormControl } from '@angular/forms';
import { FolderService } from '../../services/folders/folder.service';

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
  folders: Observable<Array<string>>;
  uploadFileForm: FormGroup;
  constructor(
     @Self() private uploadFileService: UploadService,
     private snackBarService: SnackBarService,
     private folderService: FolderService
    ) {
  }

  ngOnInit() {
    this.folders = this.folderService.getFolders()
    this.uploadFileForm = new FormGroup({
      files: new FormControl(),
      folder: new FormControl(),
    });
  }

  onFilesAdded(files: Set<File>) {
    pipe(
      fromNullable(this.uploadFileService.upload(files)),
      fold(noop, (fileInfo) => this.processUploadResults(fileInfo))
    );
  }
  loadFiles() {
    console.log(this.uploadFileForm.value)
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
