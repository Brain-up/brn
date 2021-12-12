import { ActivatedRoute, Router } from '@angular/router';
import { AdminApiService } from '@admin/services/api/admin-api.service';
import { BehaviorSubject, Subject, Subscription } from 'rxjs';
import { DataShareService } from '@shared/services/data-share.service';
import { finalize, takeUntil } from 'rxjs/operators';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { MatTableDataSource } from '@angular/material/table';
import { User, UserMapped } from '@admin/models/user.model';
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

  public dataSource: MatTableDataSource<UserMapped>;
  public readonly displayedColumns: string[] = [
    'name',
    'firstDone',
    'lastDone',
    'currentWeek',
    'workDayByLastMonth',
    'progress',
    'isFavorite',
  ];
  public readonly isLoading$ = new BehaviorSubject(true);
  public userList: UserMapped[];

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
      .getUsers()
      .pipe(
        finalize(() => this.isLoading$.next(false)),
        takeUntil(this.destroyer$),
      )
      .subscribe((userList) => {
        this.userList = userList;
        this.dataSource = new MatTableDataSource(userList);
        // Change detection cycle: ViewChild is undefined due *ngIf
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

  public favoriteFilter(filterActive: boolean): void {
    if (filterActive) {
      const favorites = this.userList.filter((user) => user.isFavorite);
      this.dataSource = new MatTableDataSource(favorites);
    } else {
      this.dataSource = new MatTableDataSource(this.userList);
    }

    this.dataSource.sort = this.sort;
    this.dataSource.paginator = this.paginator;
  }

  public navigateToSelectedUser(user): void {
    this.dataShareService.addData(user);
    this.router.navigate([user.id, 'statistics'], {
      relativeTo: this.activatedRoute,
    });
  }
}
