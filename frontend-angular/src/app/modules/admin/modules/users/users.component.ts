import { SortType } from '@admin/models/sort';
import { UsersData } from '@admin/models/users-data';
import { AdminApiService } from '@admin/services/api/admin-api.service';
import { ChangeDetectionStrategy, Component, OnDestroy, OnInit } from '@angular/core';
import { FormControl } from '@angular/forms';
import { DEBOUNCE_TIME_IN_MS } from '@shared/constants/time-constants';
import { BehaviorSubject, Subject, Subscription } from 'rxjs';
import { debounceTime, finalize, takeUntil } from 'rxjs/operators';

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
  public readonly searchControl = new FormControl();
  public readonly getUsersOptions: { pageNumber: number; sortByName: SortType; isFavorite: boolean } = {
    pageNumber: 1,
    sortByName: 'asc',
    isFavorite: false,
  };

  public usersData: UsersData;

  constructor(private readonly adminApiService: AdminApiService) {}

  ngOnInit(): void {
    this.searchControl.valueChanges
      .pipe(debounceTime(DEBOUNCE_TIME_IN_MS), takeUntil(this.destroyer$))
      .subscribe(() => {
        this.getUsersOptions.pageNumber = 1;
        this.getUsers();
      });

    this.getUsers();
  }

  ngOnDestroy(): void {
    this.destroyer$.next();
    this.destroyer$.complete();
  }

  public toggleFavorite(isFavorite: boolean): void {
    this.getUsersOptions.pageNumber = 1;
    this.getUsersOptions.isFavorite = isFavorite;

    this.getUsers();
  }

  public sortByName(sort: SortType): void {
    this.getUsersOptions.pageNumber = 1;
    this.getUsersOptions.sortByName = sort;

    this.getUsers();
  }

  public selectPage(pageNumber: number): void {
    this.getUsersOptions.pageNumber = pageNumber;

    this.getUsers();
  }

  private getUsers(): void {
    this.getUsersSubscription?.unsubscribe();

    this.isLoading$.next(true);

    this.getUsersSubscription = this.adminApiService
      .getUsers({
        pageNumber: this.getUsersOptions.pageNumber,
        sortBy: {
          name: this.getUsersOptions.sortByName,
        },
        filters: {
          isFavorite: this.getUsersOptions.isFavorite,
          search: this.searchControl.value,
        },
        withAnalytics: true,
      })
      .pipe(
        finalize(() => this.isLoading$.next(false)),
        takeUntil(this.destroyer$)
      )
      .subscribe((usersData) => (this.usersData = usersData));
  }
}
