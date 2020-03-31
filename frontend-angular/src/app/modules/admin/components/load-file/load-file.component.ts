import { ChangeDetectionStrategy, Component, OnInit, Self } from '@angular/core';
import { Observable} from 'rxjs';
import { mergeMap } from 'rxjs/operators';
import { FormGroup, FormControl } from '@angular/forms';
import { FolderService } from '../../services/folders/folder.service';
import { UploadService } from '../../services/upload/upload.service';
import { Router } from '@angular/router';
import { SnackBarService } from 'src/app/modules/shared/services/snack-bar/snack-bar.service';
import { Store } from '@ngrx/store';
import { AppStateModel } from 'src/app/models/app-state.model';
import { fetchFoldersRequest } from '../../ngrx/actions';
import { selectFolders } from '../../ngrx/reducers';
import { AdminStateModel } from '../../model/admin-state.model';

@Component({
  selector: 'app-load-file',
  templateUrl: './load-file.component.html',
  styleUrls: ['./load-file.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class LoadFileComponent implements OnInit {
  folders$: Observable<Array<string>>;
  uploadFileForm: FormGroup;
  constructor(
    private snackBarService: SnackBarService,
    private folderService: FolderService,
    private uploadService: UploadService,
    private router: Router,
    private store: Store<AdminStateModel>
  ) {
  }

  ngOnInit() {
    this.store.dispatch(fetchFoldersRequest());
    this.folders$ = this.store.select(selectFolders);
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

