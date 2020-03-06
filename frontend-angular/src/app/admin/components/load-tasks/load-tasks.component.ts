import {ChangeDetectionStrategy, Component, OnInit, OnDestroy} from '@angular/core';
import {AdminService} from '../../services/admin.service';
import {iif, Observable, of, EMPTY} from 'rxjs';
import {Group, Series} from '../../model/model';
import { SeriesModel } from '../../model/series.model';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {switchMap, tap, pluck} from 'rxjs/operators';
import {untilDestroyed} from 'ngx-take-until-destroy';
import { UploadService  } from '../../services/upload/upload.service';
import { SnackBarService } from 'src/app/shared/services/snack-bar/snack-bar.service';
import { HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';
import { FormatService } from '../../services/format/format.service';
import { LoadTasksReturnData } from '../../model/load-tasks-return-data.model';

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

  constructor(private adminAPI: AdminService,
              private fb: FormBuilder,
              private uploadFileService: UploadService,
              private router: Router,
              private snackBarService: SnackBarService,
              private formatService: FormatService) {
  }
  onSubmit() {
    const formData = new FormData();
    formData.append('taskFile', this.tasksGroup.get('file').value);
    formData.append('seriesId', this.tasksGroup.get('series').value.id);
    this.uploadFileService.sendFormData('/api/loadTasksFile?seriesId=1', formData).subscribe(returnData => {
      // TODO: - Check for other type of errors
      this.snackBarService.showHappySnackbar('Successfully loaded ' + this.tasksGroup.get('file').value.name);
      this.router.navigateByUrl('/home');
    }, (error: HttpErrorResponse) => {
      const errorObj = error.error as LoadTasksReturnData;
      this.snackBarService.showSadSnackbar(errorObj.errors[0]);
    });
  }
  ngOnDestroy() {}
  ngOnInit() {
    this.tasksGroup = this.fb.group({
      group: ['', Validators.required],
      series: [{value: '', disabled: true}, Validators.required],
      file: [null, Validators.required]
    });

    this.groups$ = this.adminAPI.getGroups();
    this.series$ = this.tasksGroup.controls.group.valueChanges.pipe(
      switchMap(({id}) => this.adminAPI.getSeriesByGroupId(id)),
    );
    this.tasksGroup.controls.series.valueChanges.pipe(
      switchMap((val: SeriesModel) => val ? this.formatService.getFormat(val.id) : EMPTY),
      pluck('data')
    ).subscribe(
      val=> this.format = val
    )
    this.tasksGroup.controls.group.statusChanges.pipe(
      switchMap(status => iif(() => status === 'VALID',
        of('').pipe(tap(_ => this.tasksGroup.controls.series.enable())),
        of('').pipe(tap(_ => this.tasksGroup.controls.series.disable())),
        ),
      ),
      untilDestroyed(this)
    ).subscribe();
  }
  onSeriesSelect(event) {
    console.log(event)
  }
}
