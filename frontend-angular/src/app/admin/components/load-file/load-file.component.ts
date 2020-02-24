import { ChangeDetectionStrategy, Component, OnInit, Self } from '@angular/core';
import { UPLOAD_DESTINATION } from '../../../shared/services/upload/upload.service';
import { fold, fromNullable } from 'fp-ts/lib/Option';
import { pipe } from 'fp-ts/lib/pipeable';
import { EMPTY, forkJoin, noop, Observable, of } from 'rxjs';
import { catchError, tap, mergeMap } from 'rxjs/operators';
import { SnackBarService } from 'src/app/shared/services/snack-bar/snack-bar.service';
import { FormGroup, FormControl } from '@angular/forms';
import { FolderService } from '../../services/folders/folder.service';
import { UploadService } from '../../services/upload/upload.service';
import { Router } from '@angular/router';

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
    //  @Self() private uploadFileService: UploadService,
    private snackBarService: SnackBarService,
    private folderService: FolderService,
    private uploadService: UploadService,
    private router: Router
  ) {
  }

  ngOnInit() {
    this.folders = this.folderService.getFolders()

    this.uploadFileForm = new FormGroup({
      files: new FormControl(),
      folder: new FormControl(),
    });
  }

  // onFilesAdded(files: Set<File>) {
  //   pipe(
  //     fromNullable(this.uploadFileService.upload(files)),
  //     fold(noop, (fileInfo) => this.processUploadResults(fileInfo))
  //   );
  // }
  loadFiles() {
    let fileName = this.uploadFileForm.value.files.name;
    this.uploadService.getUploadData(this.uploadFileForm.value.folder, fileName).pipe(
      mergeMap(data => {
        const formData = new FormData();
       
        data.data.input.forEach(input => {
          var key = Object.keys(input)[0];
          formData.append(key, input[key]);
        })
        formData.append('file', this.uploadFileForm.value.files)
        console.log(data.data.action);
        return this.uploadService.sendFormData(data.data.action, formData)
      })
    ).pipe(
      tap((resp) => {
        console.log(resp)
      })
    ).subscribe(_=> {
      this.router.navigateByUrl('/home');
      this.snackBarService.showHappySnackbar(`${fileName} was successfully uploaded`)
    })
  }

  private processUploadResults(fileInfo: { [key: string]: { progress: Observable<number> } }) {
    forkJoin(Object.values(fileInfo).map(({ progress }) => progress))
      .pipe(
        tap(() => this.snackBarService.showHappySnackbar(`${Object.keys(fileInfo).join(',')} was successfully uploaded`)),
        catchError(err => {
          this.snackBarService.showSadSnackbar(err);
          return EMPTY;
        })
      )
      .subscribe();
  }
}

