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
import { catchError, filter, map, startWith, switchMap, takeUntil, tap } from 'rxjs/operators';
import { GroupApiService } from '@admin/services/api/group-api.service';
import { SeriesApiService } from '@admin/services/api/series-api.service';
import { SubGroupApiService } from '@admin/services/api/sub-group-api.service';
import { LANGUAGES } from './languages';
import { Language } from '../../../models/language';
import { Group } from '@admin/models/group';
import { Series } from '@admin/models/series';
import { Subgroup } from '@admin/models/subgroup';
import { AdminApiService } from '@admin/services/api/admin-api.service';

enum DEFAULT_SELECT_VALUE {
 languageId = 'ru-ru',
 groupId = 2,
 seriesIdName = '1;Слова'
}

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
  @Output() subGroupChanged = new EventEmitter<number>();

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

  constructor(
    private readonly groupApiService: GroupApiService,
    private readonly seriesApiService: SeriesApiService,
    private readonly subGroupApiService: SubGroupApiService,
    private readonly adminApiService: AdminApiService
  ) {}

  ngOnInit(): void {
    this.initGroups();
    this.initSeries();
    this.initSubGroups();
    this.onSubGroupChanged();
    this.setDefaultSelectValues();
  }

  ngOnDestroy(): void {
    this.ngUnsubscribe.next();
    this.ngUnsubscribe.complete();
  }

  private initGroups(): void {
    this.groups$ = this.languagesControl.valueChanges.pipe(
      startWith(DEFAULT_SELECT_VALUE.languageId),
      tap((languageId: string) => {
        this.groupsControl.reset();
        return languageId;
      }),
      switchMap((languageId: string) => {
        return languageId ?
          this.groupApiService.getGroups(languageId)
          : of([]);
      }),
      catchError(err => {
        console.error(this.LOG_SOURCE, 'An error occurred during getGroups', err);
        return EMPTY;
      }),
      takeUntil(this.ngUnsubscribe)
    );
  }

  private initSeries(): void {
    this.series$ = this.groupsControl.valueChanges.pipe(
      startWith(DEFAULT_SELECT_VALUE.groupId),
      tap((groupId: number) => {
        this.seriesControl.reset();
        this.groupChanged.emit('' + groupId);
        return groupId;
      }),
      switchMap((groupId: number) => {
        return groupId ?
          this.seriesApiService.getSeriesByGroupId(groupId)
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
      startWith(DEFAULT_SELECT_VALUE.seriesIdName),
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
          this.subGroupApiService.getSubgroupsBySeriesId(seriesId)
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
      map((id: string) => {
        const subGroupId = Number(id);
        this.subGroupChanged.emit(subGroupId);
        return subGroupId;
      }),
      filter<number>(Boolean),
      switchMap((subGroupId) => this.adminApiService.getExercisesBySubGroupId(subGroupId)),
      catchError(err => {
        console.error(this.LOG_SOURCE, 'An error occurred during getExercisesBySubGroupId', err);
        return EMPTY;
      }),
      takeUntil(this.ngUnsubscribe)
    ).subscribe();
  }

  private setDefaultSelectValues() {
    setTimeout(() => {
      this.setDefaultLanguage();
      this.setDefaultGroup();
      this.setDefaultSeries();
    }, 0);
  }

  private setDefaultLanguage() {
    this.languagesControl.setValue(DEFAULT_SELECT_VALUE.languageId, {emitEvent: false});
  }

  private setDefaultGroup() {
    this.groupsControl.setValue(DEFAULT_SELECT_VALUE.groupId, {emitEvent: false});
  }

  private setDefaultSeries() {
    this.seriesControl.setValue(DEFAULT_SELECT_VALUE.seriesIdName, {emitEvent: false});
  }
}
