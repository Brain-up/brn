import { ChangeDetectionStrategy, Component, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';

import { Store } from '@ngrx/store';
import { iif, Observable, of, EMPTY, Subject } from 'rxjs';
import { switchMap, tap, pluck, takeUntil } from 'rxjs/operators';

import { Series } from '../../model/series';
import { Group } from '../../model/group';
import { SeriesModel } from '../../model/series.model';
import { UploadService } from '../../services/upload/upload.service';
import { FormatService } from '../../services/format/format.service';
import { LoadTasksReturnData } from '../../model/load-tasks-return-data';
import { SnackBarService } from 'src/app/modules/shared/services/snack-bar/snack-bar.service';
import { AdminService } from '../../services/admin/admin.service';
import { AdminStateModel } from '../../model/admin-state.model';
import { fetchGroupsRequest } from '../../ngrx/actions';
import { selectGroups } from '../../ngrx/reducers';

@Component({
  selector: 'app-load-tasks',
  templateUrl: './load-tasks.component.html',
  styleUrls: ['./load-tasks.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class LoadTasksComponent implements OnInit, OnDestroy {
  format: string;
  groups$: Observable<Group[]>;
  tasksGroup: FormGroup;
  series$: Observable<Series[]>;
  ngUnsubscribe = new Subject<void>();

  constructor(
    private adminAPI: AdminService,
    private fb: FormBuilder,
    private uploadFileService: UploadService,
    private router: Router,
    private snackBarService: SnackBarService,
    private formatService: FormatService,
    private store: Store<AdminStateModel>
  ) {
  }

  onSubmit() {
    const formData = new FormData();
    formData.append('taskFile', this.tasksGroup.get('file').value);
    formData.append('seriesId', this.tasksGroup.get('series').value.id);

    this.uploadFileService.sendFormData('/api/admin/loadTasksFile?seriesId=1', formData)
      .pipe(takeUntil(this.ngUnsubscribe))
      .subscribe(returnData => {
        // TODO: - Check for other type of errors
        this.snackBarService.showHappySnackbar('Successfully loaded ' + this.tasksGroup.get('file').value.name);
        this.router.navigateByUrl('/home');
      }, (error: HttpErrorResponse) => {
        const errorObj = error.error as LoadTasksReturnData;
        this.snackBarService.showSadSnackbar(errorObj.errors[0]);
      });
  }

  ngOnInit() {
    this.tasksGroup = this.fb.group({
      group: ['', Validators.required],
      series: [{value: '', disabled: true}, Validators.required],
      file: [null, Validators.required]
    });
    this.store.dispatch(fetchGroupsRequest());
    this.groups$ = this.store.select(selectGroups);

    this.series$ = this.tasksGroup.controls.group.valueChanges.pipe(
      switchMap(({id}) => this.adminAPI.getSeriesByGroupId(id)),
    );

    this.tasksGroup.controls.series.valueChanges.pipe(
      switchMap((val: SeriesModel) => val ? this.formatService.getFormat(val.id) : EMPTY),
      pluck('data'),
      takeUntil(this.ngUnsubscribe)
    ).subscribe(val => this.format = val);

    this.tasksGroup.controls.group.statusChanges.pipe(
      switchMap(status => iif(() => status === 'VALID',
        of('').pipe(tap(_ => this.tasksGroup.controls.series.enable())),
        of('').pipe(tap(_ => this.tasksGroup.controls.series.disable())),
        ),
      ),
      takeUntil(this.ngUnsubscribe)
    ).subscribe();
  }

  ngOnDestroy(): void {
    this.ngUnsubscribe.next();
    this.ngUnsubscribe.complete();
  }
}
