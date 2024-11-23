import { ActivatedRoute, Router } from '@angular/router';
import { AdminApiService } from '@admin/services/api/admin-api.service';
import { BehaviorSubject, Subject, Subscription } from 'rxjs';
import { finalize, takeUntil } from 'rxjs/operators';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { MatTableDataSource } from '@angular/material/table';
import { TokenService } from '@root/services/token.service';
import { User, UserMapped } from '@admin/models/user.model';
import { ChangeDetectionStrategy, Component, OnDestroy, OnInit, ViewChild, inject } from '@angular/core';

@Component({
    selector: 'app-users',
    templateUrl: './users.component.html',
    styleUrls: ['./users.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    standalone: false
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

  @ViewChild(MatSort) set sort(sort: MatSort) {
    this.sorting = sort;
    if (this.sorting) {
      this.dataSource.sort = this.sorting;
    }
  }
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
