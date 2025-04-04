import { Answer, Exercise } from '@admin/models/exercise';
import { AdminApiService } from '@admin/services/api/admin-api.service';

import { ChangeDetectionStrategy, ChangeDetectorRef, Component, inject, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatSelectModule } from '@angular/material/select';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatSort, MatSortModule } from '@angular/material/sort';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { TranslateModule } from '@ngx-translate/core';
import { BehaviorSubject, combineLatest, filter, map, Subject, Subscription, switchMap, tap } from 'rxjs';
import { SelectPanelComponent } from './components/select-panel/select-panel.component';
import { Task } from '@admin/models/exercise';

@Component({
  selector: 'app-exercises',
  templateUrl: './exercises.component.html',
  styleUrls: ['./exercises.component.scss'],
  imports: [
    MatFormFieldModule,
    MatIconModule,
    MatSelectModule,
    MatSlideToggleModule,
    MatSortModule,
    MatTableModule,
    ReactiveFormsModule,
    TranslateModule,
    SelectPanelComponent
],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ExercisesComponent implements OnInit, OnDestroy {
  private readonly adminApiService = inject(AdminApiService);
  private readonly cdr = inject(ChangeDetectorRef);

  // TODO: Skipped for migration because:
  //  Class of this query is manually instantiated. This is discouraged and prevents
  //  migration.
  @ViewChild(MatSort) sort: MatSort;
  groupId: string;
  seriesId: string;
  subGroupId: string;
  showExercises: boolean;
  displayedColumns: string[];
  dataSource: MatTableDataSource<any>;

  seriesName$ = new BehaviorSubject<string>('');
  private groupId$ = new Subject<string>();
  private subGroupId$ = new Subject<number>();
  private subscription: Subscription;

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

  onSubGroupChange(subGroupId: number): void {
    this.subGroupId$.next(subGroupId);
  }

  ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }

  isEnableChanged(exercise, isEnable: boolean) {
    console.log('enable changed:', exercise, isEnable);
  }

  // TODO: implement a real logic for task matrix (for now it is a draft)
  getMatrixFromTasks(tasks: Task[]): string {
    if (!tasks || tasks.length === 0) {
      return '';
    }

    let res = '';
    tasks.forEach((task: Task) => {
      const row = task.answerOptions.map((answer: Answer) => answer.word).join(' ');
      res += row + '\n';
    });

    return res;
  }

  private initDataSource(): void {
    this.displayedColumns = ['id', 'seriesId', 'name', 'level', 'noise', 'noiseSound', 'tasks', 'available'];
  }

  private initExercises() {
    this.subscription = combineLatest([this.groupId$, this.seriesName$, this.subGroupId$]).pipe(
      map((argsArray) => {
        const allIdsExist = argsArray.every(x => !!x);
        if (!allIdsExist) {
          this.hideExercisesTable();
        }
        return allIdsExist ? argsArray[2] : false;
      }),
      filter<number>(Boolean),
      tap(_ => (this.showExercises = true)),
      switchMap((subGroupId) => this.adminApiService.getExercisesBySubGroupId(subGroupId))
    ).subscribe((exercises) => {
      this.setDataSource(exercises);
      this.cdr.detectChanges();
    });
  }

  private hideExercisesTable(): void {
    this.showExercises = false;
  }

  private setDataSource(exercises: Exercise[]): void {
    this.dataSource = new MatTableDataSource(exercises);
    this.dataSource.sort = this.sort;
  }
}
