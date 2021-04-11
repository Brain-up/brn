import { Component, OnInit, Output, EventEmitter } from '@angular/core';
import { FormControl } from '@angular/forms';
import { catchError, switchMap, take, tap } from 'rxjs/operators';

import { EMPTY, Observable } from 'rxjs';
import { Group } from '../../../model/group';
import { Series } from '../../../model/series';
import { Subgroup } from '../../../model/subgroup';
import { AdminService } from '../../../services/admin/admin.service';

@Component({
  selector: 'app-select-panel',
  templateUrl: './select-panel.component.html',
  styleUrls: ['./select-panel.component.scss']
})
export class SelectPanelComponent implements OnInit {
  @Output() groupChanged = new EventEmitter<string>();
  @Output() seriesChanged = new EventEmitter<string>();
  @Output() subGroupChanged = new EventEmitter<string>();

  groups$: Observable<Group[]>;
  series$: Observable<Series[]>;
  subGroups$: Observable<Subgroup[]>;

  groupsControl = new FormControl();
  seriesControl = new FormControl();
  subGroupsControl = new FormControl();
  private readonly LOG_SOURCE = 'SelectPanelComponent';

  constructor(private adminService: AdminService) {
  }

  ngOnInit(): void {
    this.groups$ = this.adminService.getGroups().pipe(
      catchError(err => {
        console.error(this.LOG_SOURCE, 'An error occurred during getGroups', err);
        return EMPTY;
      }),
      take(1),
      tap(x => console.log('groups', x))
    );

    this.series$ = this.groupsControl.valueChanges.pipe(
      tap((id: string) => {
        this.groupChanged.emit(id);
      }),
      switchMap(id => this.adminService.getSeriesByGroupId(id)),
      catchError(err => {
        console.error(this.LOG_SOURCE, 'An error occurred during getSeriesByGroupId', err);
        return EMPTY;
      }),
      take(1),
    );

    this.subGroups$ = this.seriesControl.valueChanges.pipe(
      tap((id: string) => {
        this.seriesChanged.emit(id);
      }),
      switchMap(id => this.adminService.getSubgroupsBySeriesId(id)),
      catchError(err => {
        console.error(this.LOG_SOURCE, 'An error occurred during getSubgroupsBySeriesId', err);
        return EMPTY;
      }),
      take(1),
    );

    this.subGroupsControl.valueChanges.pipe(
      tap((id: string) => {
        this.subGroupChanged.emit(id);
      }),
      switchMap(id => this.adminService.getExercisesBySubGroupId(id)),
      catchError(err => {
        console.error(this.LOG_SOURCE, 'An error occurred during getExercisesBySubGroupId', err);
        return EMPTY;
      }),
      take(1),
    ).subscribe();
  }
}
