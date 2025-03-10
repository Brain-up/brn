import { User, UserMapped } from '@admin/models/user.model';
import { AdminApiService } from '@admin/services/api/admin-api.service';
import { CommonModule, DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, OnDestroy, OnInit, ViewChild, inject } from '@angular/core';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginator, MatPaginatorModule } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatSort, MatSortModule } from '@angular/material/sort';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { TokenService } from '@root/services/token.service';
import { BarChartComponent } from '@shared/components/bar-chart/bar-chart.component';
import { DurationTransformPipe } from '@shared/pipes/duration-transform.pipe';
import { ShortNamePipe } from '@shared/pipes/short-name.pipe';
import { BehaviorSubject, Subject, Subscription } from 'rxjs';
import { finalize, takeUntil } from 'rxjs/operators';

@Component({
  selector: 'app-users',
  templateUrl: './users.component.html',
  styleUrls: ['./users.component.scss'],
  imports: [
    CommonModule,
    MatButtonToggleModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatPaginatorModule,
    MatProgressBarModule,
    MatSlideToggleModule,
    MatSortModule,
    MatTableModule,
    TranslateModule,
    BarChartComponent,
    DatePipe,
    DurationTransformPipe,
    ShortNamePipe,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class UsersComponent implements OnInit, OnDestroy {
  private activatedRoute = inject(ActivatedRoute);
  private readonly adminApiService = inject(AdminApiService);
  private router = inject(Router);
  private tokenService = inject(TokenService);

  private readonly destroyer$ = new Subject<void>();
  private getUsersSubscription: Subscription;
  private sorting: MatSort;
  private paging: MatPaginator;

  public dataSource: MatTableDataSource<UserMapped>;
  public readonly displayedColumns: string[] = [
    'name',
    'email',
    'firstDone',
    'lastDone',
    'lastVisit',
    'currentWeek',
    'spentTime',
    'doneExercises',
    'studyDaysInCurrentMonth',
    'progress',
    'isFavorite',
  ];
  public readonly isLoading$ = new BehaviorSubject(true);
  public userList: UserMapped[];

  // TODO: Skipped for migration because:
  //  Accessor queries cannot be migrated as they are too complex.
  @ViewChild(MatSort) set sort(sort: MatSort) {
    this.sorting = sort;
    if (this.sorting) {
      this.dataSource.sort = this.sorting;
    }
  }
  // TODO: Skipped for migration because:
  //  Accessor queries cannot be migrated as they are too complex.
  @ViewChild(MatPaginator) set paginator(paginator: MatPaginator) {
    this.paging = paginator;
    if (this.paging) {
      this.dataSource.paginator = this.paging;
    }
  }

  public ngOnInit(): void {
    this.getUsers();
  }

  public ngOnDestroy(): void {
    this.destroyer$.next();
    this.destroyer$.complete();
  }

  private getUsers(): void {
    this.getUsersSubscription?.unsubscribe();
    this.isLoading$.next(true);

    this.getUsersSubscription = this.adminApiService
      .getUsers()
      .pipe(
        finalize(() => this.isLoading$.next(false)),
        takeUntil(this.destroyer$),
      )
      .subscribe((userList) => {
        this.userList = userList;
        this.dataSource = new MatTableDataSource(userList);
      });
  }

  public applyFilter(event: Event): void {
    const filterValue = (event.target as HTMLInputElement).value;
    this.dataSource.filter = filterValue.trim().toLowerCase();

    if (this.dataSource.paginator) {
      this.dataSource.paginator.firstPage();
    }
  }

  public favoriteFilter(filterActive: boolean): void {
    if (filterActive) {
      const favorites = this.userList.filter((user) => user.isFavorite);
      this.dataSource = new MatTableDataSource(favorites);
    } else {
      this.dataSource = new MatTableDataSource(this.userList);
    }
  }

  public navigateToSelectedUser(user: User): void {
    this.tokenService.setToken<User>(user, 'SELECTED_USER');
    this.router.navigate([user.id, 'statistics'], {
      relativeTo: this.activatedRoute,
    });
  }
}
