import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  OnDestroy,
  OnInit
} from '@angular/core';
import { BehaviorSubject, combineLatest, Subject, Subscription } from 'rxjs';
import { map, filter, switchMap, tap } from 'rxjs/operators';

import { AdminService } from '../../services/admin/admin.service';
import { Exercise } from '../../model/exercise';

@Component({
  selector: 'app-exercises',
  templateUrl: './exercises.component.html',
  styleUrls: ['./exercises.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ExercisesComponent implements OnInit, OnDestroy {
  exercises: Exercise[];
  groupId: string;
  seriesId: string;
  subGroupId: string;
  showExercises: boolean;
  displayedColumns: string[];

  seriesName$ = new BehaviorSubject<string>('');
  private groupId$ = new Subject<string>();
  private subGroupId$ = new Subject<string>();
  private subscription: Subscription;
  private readonly LOG_SOURCE = 'ExercisesComponent';

  constructor(
    private adminService: AdminService,
    private cdr: ChangeDetectorRef
  ) {
  }

  ngOnInit(): void {
    this.initDataSource();
    this.initExercises();
  }

  onGroupChange(groupId: string): void {
    this.groupId$.next(groupId);
  }

  onSeriesChange(seriesName: string): void {
    this.seriesName$.next(seriesName);
  }

  onSubGroupChange(subGroupId: string): void {
    this.subGroupId$.next(subGroupId);
  }

  ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }

  isEnableChanged(exercise, isEnable: boolean) {
    console.log('enable changed:', exercise, isEnable);
  }

  private initDataSource(): void {
    this.displayedColumns = [ 'id', 'seriesId', 'name', 'level', 'noise', 'noiseSound', 'tasks', 'available'];
  }

  private initExercises() {
    this.subscription = combineLatest([this.groupId$, this.seriesName$, this.subGroupId$]).pipe(
      map((argsArray: string[]) => {
        const allIdsExist = argsArray.every(x => !!x);
        if (!allIdsExist) {
          this.hideExercisesTable();
        }
        return allIdsExist ? argsArray[2] : false;
      }),
      filter(Boolean),
      tap(_ => {
        this.showExercises = true;
      }),
      switchMap((subGroupId: string) => this.adminService.getExercisesBySubGroupId(subGroupId))
    ).subscribe((res) => {
      this.exercises = res;
      this.cdr.detectChanges();
    });
  }

  private hideExercisesTable(): void {
    this.showExercises = false;
  }
}
