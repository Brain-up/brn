import {ChangeDetectionStrategy, Component, OnDestroy, OnInit, Self} from '@angular/core';
import {AdminService} from '../../services/admin.service';
import {EMPTY, forkJoin, iif, noop, Observable, of} from 'rxjs';
import {Group, Series} from '../../model/model';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {catchError, switchMap, tap} from 'rxjs/operators';
import {UPLOAD_DESTINATION, UploadService} from '../../../shared/services/upload/upload.service';
import {untilDestroyed} from 'ngx-take-until-destroy';
import {MatSnackBar} from '@angular/material/snack-bar';
import {pipe} from 'fp-ts/lib/pipeable';
import {fold, fromNullable} from 'fp-ts/lib/Option';
import {showHappySnackbar, showSadSnackbar} from '../../../shared/pure';
import { UploadService as UploadService2 } from '../../services/upload/upload.service';

@Component({
  selector: 'app-load-tasks',
  templateUrl: './load-tasks.component.html',
  styleUrls: ['./load-tasks.component.scss'],
  providers: [
    {
      provide: UPLOAD_DESTINATION,
      useValue: '/api/loadTasksFile'
    },
    UploadService
  ],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class LoadTasksComponent implements OnInit, OnDestroy {
  groups$: Observable<Group[]>;
  tasksGroup: FormGroup;
  series$: Observable<Series[]>;

  constructor(private adminAPI: AdminService,
              private fb: FormBuilder,
              @Self() private uploadFileService: UploadService,
              private uploadFileService2: UploadService2,
              private snackbar: MatSnackBar) {
  }
  onSubmit() {
    let formData = new FormData();
    formData.append('taskFile', this.tasksGroup.get('file').value);
    formData.append('seriesId', this.tasksGroup.get('series').value.id);
    this.uploadFileService2.sendFormData('/api/loadTasksFile?seriesId=1',formData).subscribe(console.log)
  }
  ngOnInit() {
    this.tasksGroup = this.fb.group({
      group: ['', Validators.required],
      series: [{value: '', disabled: true}, Validators.required],
      file: [null, Validators.required]
    });
    this.groups$ = this.adminAPI.getGroups();
    this.series$ = this.tasksGroup.controls.group.valueChanges.pipe(
      tap(data=> console.log(data)),
      switchMap(({id}) => this.adminAPI.getSeriesByGroupId(id)),
    );
    this.tasksGroup.controls.group.statusChanges.pipe(
      switchMap(status => iif(() => status === 'VALID',
        of('').pipe(tap(_ => this.tasksGroup.controls.series.enable())),
        of('').pipe(tap(_ => this.tasksGroup.controls.series.disable())),
        ),
      ),
      untilDestroyed(this)
    ).subscribe();
  }

  ngOnDestroy(): void {}

  onFilesAdded(files: Set<File>) {
    pipe(
      fromNullable(this.uploadFileService.upload(files, {seriesId: this.tasksGroup.controls.series.value.id})),
      fold(noop, (fileInfo) => this.processUploadResults(fileInfo))
    );
  }

  private processUploadResults(fileInfo: { [key: string]: { progress: Observable<number> } }) {
    forkJoin(Object.values(fileInfo).map(({progress}) => progress))
      .pipe(
        tap(showHappySnackbar.bind(null, this.snackbar, `${Object.keys(fileInfo).join(',')} was successfully uploaded`)),
        catchError(err => {
          showSadSnackbar.bind(null, this.snackbar)(err);
          return EMPTY;
        })
      )
      .subscribe();
  }
}
