import {ChangeDetectionStrategy, Component, OnDestroy, OnInit, Self} from '@angular/core';
import {AdminService} from '../../services/admin.service';
import {iif, Observable, of} from 'rxjs';
import {Group, Series} from '../../model/model';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {switchMap, tap} from 'rxjs/operators';
import {UPLOAD_DESTINATION, UploadService} from '../../../shared/upload-file/service/upload.service';
import {untilDestroyed} from 'ngx-take-until-destroy';

@Component({
  selector: 'app-load-tasks',
  templateUrl: './load-tasks.component.html',
  styleUrls: ['./load-tasks.component.scss'],
  providers: [
    {
      provide: UPLOAD_DESTINATION,
      useValue: '/loadTasks'
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
              @Self() private uploadFileService: UploadService) {
  }

  ngOnInit() {
    this.tasksGroup = this.fb.group({
      group: ['', Validators.required],
      series: [{value: '', disabled: true}, Validators.required]
    });
    this.groups$ = this.adminAPI.getGroups();
    this.series$ = this.tasksGroup.controls.group.valueChanges.pipe(
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

  ngOnDestroy(): void {
  }

}
