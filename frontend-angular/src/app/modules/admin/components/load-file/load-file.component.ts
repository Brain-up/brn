import { ChangeDetectionStrategy, Component, OnDestroy, OnInit } from '@angular/core';
import { FormGroup, FormControl } from '@angular/forms';
import { Router } from '@angular/router';

import { Store } from '@ngrx/store';
import { Observable, Subject } from 'rxjs';
import { mergeMap, takeUntil } from 'rxjs/operators';

import { FoldersService } from '../../services/folders/folders.service';
import { UploadService } from '../../services/upload/upload.service';
import { SnackBarService } from 'src/app/modules/shared/services/snack-bar/snack-bar.service';
import { fetchFoldersRequest } from '../../ngrx/actions';
import { selectFolders } from '../../ngrx/reducers';
import { AdminStateModel } from '../../model/admin-state.model';

@Component({
  selector: 'app-load-file',
  templateUrl: './load-file.component.html',
  styleUrls: ['./load-file.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class LoadFileComponent implements OnInit, OnDestroy {
  folders$: Observable<Array<string>>;
  uploadFileForm: FormGroup;
  ngUnsubscribe = new Subject<void>();

  constructor(
    private snackBarService: SnackBarService,
    private folderService: FoldersService,
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
      takeUntil(this.ngUnsubscribe)
    ).subscribe(_ => {
      this.router.navigateByUrl('/home');
      this.snackBarService.showHappySnackbar(`${fileName} was successfully uploaded`);
    });
  }

  ngOnDestroy(): void {
    this.ngUnsubscribe.next();
    this.ngUnsubscribe.complete();
  }
}

