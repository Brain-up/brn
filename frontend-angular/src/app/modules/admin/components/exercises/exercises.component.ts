import { ChangeDetectionStrategy, Component, OnDestroy, OnInit } from '@angular/core';
import { combineLatest, Observable, Subject } from 'rxjs';
import { map, filter, switchMap } from 'rxjs/operators';

import { AdminService } from '../../services/admin/admin.service';
import { Exercise } from '../../model/exercise';

@Component({
  selector: 'app-exercises',
  templateUrl: './exercises.component.html',
  styleUrls: ['./exercises.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ExercisesComponent implements OnInit, OnDestroy {
  exercises$: Observable<Exercise[]>;
  groupId: string;
  seriesId: string;
  subGroupId: string;
  private groupId$ = new Subject<string>();
  private seriesId$ = new Subject<string>();
  private subGroupId$ = new Subject<string>();
  private readonly LOG_SOURCE = 'ExercisesComponent';

  constructor(
    private adminService: AdminService
  ) {
  }

  ngOnInit(): void {
    this.exercises$ = combineLatest([this.groupId$, this.seriesId$, this.subGroupId$]).pipe(
      map((argsArray: string[]) => {
        const allIdsExist = argsArray.every(x => !!x);
        if (!allIdsExist) {
          this.hideExercisesTable();
        }
        return allIdsExist ? argsArray[2] : false;
      }),
      filter(Boolean),
      switchMap((subGroupId: string) => this.adminService.getExercisesBySubGroupId(subGroupId))
    );
  }

  onGroupChange(groupId: string) {
    this.groupId$.next(groupId);
  }

  onSeriesChange(seriesId: string) {
    this.seriesId$.next(seriesId);
  }

  onSubGroupChange(subGroupId: string) {
    this.subGroupId$.next(subGroupId);
  }

  ngOnDestroy(): void {
  }

  private hideExercisesTable() {
    console.log('Hide exercises table');
  }
}
