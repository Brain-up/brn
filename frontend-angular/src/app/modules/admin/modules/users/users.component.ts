import { ActivatedRoute, Router } from '@angular/router';
import { AdminApiService } from '@admin/services/api/admin-api.service';
import { BehaviorSubject, Subject, Subscription } from 'rxjs';
import { DataShareService } from '@shared/services/data-share.service';
import { finalize, takeUntil } from 'rxjs/operators';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { MatTableDataSource } from '@angular/material/table';
import {
  User,
  UserMapped,
  UserWithNoAnalytics,
} from '@admin/models/user.model';
import {
  ChangeDetectionStrategy,
  Component,
  OnDestroy,
  OnInit,
  ViewChild,
} from '@angular/core';

@Component({
  selector: 'app-users',
  templateUrl: './users.component.html',
  styleUrls: ['./users.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UsersComponent implements OnInit, OnDestroy {
  private readonly destroyer$ = new Subject<void>();
  private getUsersSubscription: Subscription;

  public readonly isLoading$ = new BehaviorSubject(true);
  public dataSource: MatTableDataSource<UserWithNoAnalytics | UserMapped>;
  public displayedColumns: string[] = [
    'name',
    'firstVisit',
    'lastVisit',
    'currentWeek',
    'workingDaysInLastMonth',
    'progress',
    'favorite',
  ];
  public filterFavorites: boolean = false;
  public userList: UserWithNoAnalytics[] | UserMapped[];

  @ViewChild(MatPaginator) paginator: MatPaginator;
  @ViewChild(MatSort) sort: MatSort;

  constructor(
    private readonly adminApiService: AdminApiService,
    private activatedRoute: ActivatedRoute,
    private dataShareService: DataShareService<User>,
    private router: Router,
  ) {}

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
      .getUsers('ROLE_USER', true)
      .pipe(
        finalize(() => this.isLoading$.next(false)),
        takeUntil(this.destroyer$),
      )
      .subscribe((userList) => {
        this.userList = userList;
        this.dataSource = new MatTableDataSource(userList);
        // Change detection cycle: ViewChild undefined due *ngIf
        setTimeout(() => {
          this.dataSource.sort = this.sort;
          this.dataSource.paginator = this.paginator;
        });
   
      });
  }

  public applyFilter(event: Event): void {
    const filterValue = (event.target as HTMLInputElement).value;
    this.dataSource.filter = filterValue.trim().toLowerCase();

    if (this.dataSource.paginator) {
      this.dataSource.paginator.firstPage();
    }
  }

  public applyFavoriteFilter(column): void {
    // this.filterFavorites = !this.filterFavorites;
    // this.filterValues[column] = filterValue;
    // this.dataSource.filter = JSON.stringify(this.filterValues);
    // if (this.dataSource.paginator) {
    //   this.dataSource.paginator.firstPage();
    // }
  }

  public navigateToSelectedUser(user): void {
    this.dataShareService.addData(user);
    this.router.navigate([user.id, 'statistics'], {
      relativeTo: this.activatedRoute,
    });
  }
}
