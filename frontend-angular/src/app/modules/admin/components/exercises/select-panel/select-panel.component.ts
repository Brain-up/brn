import {
  ChangeDetectionStrategy,
  Component,
  EventEmitter,
  Input,
  OnDestroy,
  OnInit,
  Output
} from '@angular/core';
import { FormControl } from '@angular/forms';
import { catchError, filter, switchMap, takeUntil, tap } from 'rxjs/operators';

import { EMPTY, Observable, of, Subject } from 'rxjs';
import { Group } from '../../../model/group';
import { Series } from '../../../model/series';
import { Subgroup } from '../../../model/subgroup';
import { AdminService } from '../../../services/admin/admin.service';

@Component({
  selector: 'app-select-panel',
  templateUrl: './select-panel.component.html',
  styleUrls: ['./select-panel.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class SelectPanelComponent implements OnInit, OnDestroy {
  @Input() set groupId(groupId: string) {
    console.log('set groupId:', groupId);
    this.groupsControl.setValue(groupId);
  }

  @Input() set seriesId(seriesId: string) {
    console.log('set seriesId:', seriesId);
    this.seriesControl.setValue(seriesId);
  }

  @Input() set subGroupId(subGroupId: string) {
    console.log('set subGroupId:', subGroupId);
    this.subGroupsControl.setValue(subGroupId);
  }

  @Output() groupChanged = new EventEmitter<string>();
  @Output() seriesChanged = new EventEmitter<string>();
  @Output() subGroupChanged = new EventEmitter<string>();

  groups$: Observable<Group[]>;
  series$: Observable<Series[]>;
  subGroups$: Observable<Subgroup[]>;

  groupsControl = new FormControl();
  seriesControl = new FormControl();
  subGroupsControl = new FormControl();
  ngUnsubscribe = new Subject<void>();
  private readonly LOG_SOURCE = 'SelectPanelComponent';

  constructor(private adminService: AdminService) {
  }

  ngOnInit(): void {
    this.initGroups();
    this.initSeries();
    this.initSubGroups();
    this.onSubGroupChanged();
  }

  ngOnDestroy(): void {
    this.ngUnsubscribe.next();
    this.ngUnsubscribe.complete();
  }

  private initGroups() {
    this.groups$ = this.adminService.getGroups().pipe(
      catchError(err => {
        console.error(this.LOG_SOURCE, 'An error occurred during getGroups', err);
        return EMPTY;
      }),
      takeUntil(this.ngUnsubscribe)
    );
  }

  private initSeries() {
    this.series$ = this.groupsControl.valueChanges.pipe(
      tap((groupId: string) => {
        this.seriesControl.reset();
        this.groupChanged.emit(groupId);
        return groupId;
      }),
      switchMap((groupId: string) => {
        return groupId ?
          this.adminService.getSeriesByGroupId(groupId)
          : of([]);
      }),
      catchError(err => {
        console.error(this.LOG_SOURCE, 'An error occurred during getSeriesByGroupId', err);
        return EMPTY;
      }),
      takeUntil(this.ngUnsubscribe)
    );
  }

  private initSubGroups() {
    this.subGroups$ = this.seriesControl.valueChanges.pipe(
      tap((seriesId: string) => {
        this.subGroupsControl.reset();
        this.seriesChanged.emit(seriesId);
        return seriesId;
      }),
      switchMap((seriesId: string) => {
        return seriesId ?
          this.adminService.getSubgroupsBySeriesId(seriesId)
          : of([]);
      }),
      catchError(err => {
        console.error(this.LOG_SOURCE, 'An error occurred during getSubgroupsBySeriesId', err);
        return EMPTY;
      }),
      takeUntil(this.ngUnsubscribe)
    );
  }

  private onSubGroupChanged() {
    this.subGroupsControl.valueChanges.pipe(
      tap((id: string) => {
        this.subGroupChanged.emit(id);
      }),
      filter(Boolean),
      switchMap((subGroupId: string) => this.adminService.getExercisesBySubGroupId(subGroupId)),
      catchError(err => {
        console.error(this.LOG_SOURCE, 'An error occurred during getExercisesBySubGroupId', err);
        return EMPTY;
      }),
      takeUntil(this.ngUnsubscribe)
    ).subscribe();
  }
}
