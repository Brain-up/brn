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
import { EMPTY, Observable, of, Subject } from 'rxjs';
import { catchError, filter, startWith, switchMap, take, takeUntil, tap } from 'rxjs/operators';

import { Group } from '../../../model/group';
import { Series } from '../../../model/series';
import { Subgroup } from '../../../model/subgroup';
import { AdminService } from '../../../services/admin/admin.service';
import { Language } from '../../../model/language';
import { LANGUAGES } from './languages';

@Component({
  selector: 'app-select-panel',
  templateUrl: './select-panel.component.html',
  styleUrls: ['./select-panel.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class SelectPanelComponent implements OnInit, OnDestroy {
  @Input() set groupId(groupId: string) {
    this.groupsControl.setValue(groupId);
  }
  @Input() set seriesId(seriesId: string) {
    this.seriesControl.setValue(seriesId);
  }
  @Input() set subGroupId(subGroupId: string) {
    this.subGroupsControl.setValue(subGroupId);
  }
  @Output() groupChanged = new EventEmitter<string>();
  @Output() seriesChanged = new EventEmitter<string>(); // emit seriesName
  @Output() subGroupChanged = new EventEmitter<string>();

  languages: Language[] = LANGUAGES;
  groups$: Observable<Group[]>;
  series$: Observable<Series[]>;
  subGroups$: Observable<Subgroup[]>;

  languagesControl = new FormControl();
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

  private initGroups(): void {
    let initialGroups: Group[];
    this.getInitialGroups$()
      .subscribe((groups: Group[]) => {
        initialGroups = groups;
      });

    this.groups$ = this.languagesControl.valueChanges.pipe(
      startWith(initialGroups),
      tap((languageId: string) => {
        console.log('languageId=', languageId);
        this.groupsControl.reset();
        return languageId;
      }),
      switchMap((languageId: string) => {
        return languageId ?
          this.adminService.getGroups(languageId)
          : of(initialGroups);
      }),
      catchError(err => {
        console.error(this.LOG_SOURCE, 'An error occurred during getGroups', err);
        return EMPTY;
      }),
      takeUntil(this.ngUnsubscribe)
    );
  }

  private getInitialGroups$(): Observable<Group[]> {
    return this.adminService.getGroups().pipe(
      catchError(err => {
        console.error(this.LOG_SOURCE, 'An error occurred during getGroups', err);
        return EMPTY;
      }),
      take(1)
    );
  }

  private initSeries(): void {
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

  private initSubGroups(): void {
    this.subGroups$ = this.seriesControl.valueChanges.pipe(
      tap((seriesIdAndName: string) => {
        this.subGroupsControl.reset();
        if (seriesIdAndName) {
          const seriesName = seriesIdAndName.split(';')[1];
          this.seriesChanged.emit(seriesName);
        }
      }),
      switchMap((seriesIdAndName: string) => {
        const seriesId = seriesIdAndName ? seriesIdAndName.split(';')[0] : null;
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

  private onSubGroupChanged(): void {
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
