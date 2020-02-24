import { ChangeDetectionStrategy, Component, OnInit, Self } from '@angular/core';
import { UPLOAD_DESTINATION } from '../../../shared/services/upload/upload.service';
import { EMPTY, forkJoin, Observable} from 'rxjs';
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
    private snackBarService: SnackBarService,
    private folderService: FolderService,
    private uploadService: UploadService,
    private router: Router
  ) {
  }

  ngOnInit() {
    this.folders = this.folderService.getFolders();

    this.uploadFileForm = new FormGroup({
      files: new FormControl(),
      folder: new FormControl(),
    });
  }
  loadFiles() {
    const fileName = this.uploadFileForm.value.files.name;
    this.uploadService.getUploadData(this.uploadFileForm.value.folder, fileName).pipe(
      mergeMap(data => {
        const formData = new FormData();
        data.data.input.forEach(input => {
          const key = Object.keys(input)[0];
          formData.append(key, input[key]);
        });
        formData.append('file', this.uploadFileForm.value.files);
        return this.uploadService.sendFormData(data.data.action, formData);
      }),
    ).subscribe(_ => {
      this.router.navigateByUrl('/home');
      this.snackBarService.showHappySnackbar(`${fileName} was successfully uploaded`);
    });
  }
}

