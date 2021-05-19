import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { Observable, Subject } from 'rxjs';
import { filter, switchMap, takeUntil } from 'rxjs/operators';
import { SnackBarService } from '@root/services/snack-bar.service';
import { Group } from '@admin/models/group';
import { Series } from '@admin/models/series';
import { AdminApiService } from '@admin/services/api/admin-api.service';
import { GroupApiService } from '@admin/services/api/group-api.service';
import { SeriesApiService } from '@admin/services/api/series-api.service';
import { TranslateService } from '@ngx-translate/core';

@Component({
  selector: 'app-load-tasks',
  templateUrl: './load-tasks.component.html',
  styleUrls: ['./load-tasks.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LoadTasksComponent implements OnInit, OnDestroy {
  private readonly destroyer$ = new Subject<void>();

  public tasksGroup: FormGroup;
  public fileFormat$: Observable<string>;
  public groups$: Observable<Group[]>;
  public series$: Observable<Series[]>;

  constructor(
    private readonly router: Router,
    private readonly formBuilder: FormBuilder,
    private readonly snackBarService: SnackBarService,
    private readonly groupApiService: GroupApiService,
    private readonly seriesApiService: SeriesApiService,
    private readonly adminApiService: AdminApiService,
    private readonly translateService: TranslateService
  ) {}

  ngOnInit(): void {
    this.tasksGroup = this.formBuilder.group({
      group: ['', Validators.required],
      series: [{ value: '', disabled: true }, Validators.required],
      file: [{ value: null, disabled: true }, Validators.required],
    });

    this.groups$ = this.groupApiService.getGroups();

    this.series$ = this.tasksGroup.controls.group.valueChanges.pipe(
      switchMap(({ id }: Series) => this.seriesApiService.getSeriesByGroupId(id))
    );

    this.fileFormat$ = this.tasksGroup.controls.series.valueChanges.pipe(
      filter<Series>(Boolean),
      switchMap(({ id }) => this.seriesApiService.getFileFormatBySeriesId(id))
    );

    this.tasksGroup.controls.group.statusChanges.pipe(takeUntil(this.destroyer$)).subscribe((status: string) => {
      const action = status === 'VALID' ? 'enable' : 'disable';
      this.tasksGroup.controls.series[action]();
    });

    this.tasksGroup.controls.series.statusChanges.pipe(takeUntil(this.destroyer$)).subscribe((status: string) => {
      const action = status === 'VALID' ? 'enable' : 'disable';
      this.tasksGroup.controls.file[action]();
    });
  }

  ngOnDestroy(): void {
    this.destroyer$.next();
    this.destroyer$.complete();
  }

  public onSubmit(): void {
    const formData = new FormData();
    formData.append('taskFile', this.tasksGroup.get('file').value);
    formData.append('seriesId', this.tasksGroup.get('series').value.id);

    this.adminApiService
      .sendFormData('/api/admin/loadTasksFile?seriesId=1', formData)
      .pipe(takeUntil(this.destroyer$))
      .subscribe(
        () => {
          this.snackBarService.success(
            this.translateService.get(
              'Admin.Modules.UploadFile.Modules.LoadTasks.SnackBar.FileSuccessfullyUploaded [fileName]',
              this.tasksGroup.get('file').value.name
            )
          );
          this.router.navigateByUrl('/');
        },
        (err: HttpErrorResponse) => {
          this.snackBarService.error(err.error.errors[0]);
        }
      );
  }
}
