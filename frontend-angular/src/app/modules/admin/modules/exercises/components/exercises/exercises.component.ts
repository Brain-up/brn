import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  OnDestroy,
  OnInit,
  ViewChild
} from '@angular/core';
import { MatTableDataSource } from '@angular/material/table';
import { MatSort } from '@angular/material/sort';
import { BehaviorSubject, combineLatest, Subject, Subscription } from 'rxjs';
import { map, filter, switchMap, tap } from 'rxjs/operators';
import { Answer, Exercise, Task } from '@admin/models/exercise';
import { AdminApiService } from '@admin/services/api/admin-api.service';

@Component({
  selector: 'app-exercises',
  templateUrl: './exercises.component.html',
  styleUrls: ['./exercises.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ExercisesComponent implements OnInit, OnDestroy {
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

  constructor(private readonly adminApiService: AdminApiService, private readonly cdr: ChangeDetectorRef) {}

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
      const row  = task.answerOptions.map((answer: Answer) => answer.word).join(' ');
      res += row + '\n';
    });

    return res;
  }

  private initDataSource(): void {
    this.displayedColumns = [ 'id', 'seriesId', 'name', 'level', 'noise', 'noiseSound', 'tasks', 'available'];
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
