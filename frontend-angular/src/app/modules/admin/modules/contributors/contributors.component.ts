import { Component, OnInit, ChangeDetectionStrategy, OnDestroy, ViewChild } from '@angular/core';
import { BehaviorSubject, Subject, Subscription } from 'rxjs';
import { MatSort } from '@angular/material/sort';
import { MatPaginator } from '@angular/material/paginator';
import { Contributor } from '@admin/models/contrubutor.model';
import { MatTableDataSource } from '@angular/material/table';
import { AdminApiService } from '@admin/services/api/admin-api.service';
import { finalize, takeUntil } from 'rxjs/operators';

@Component({
  selector: 'app-contributors',
  templateUrl: './contributors.component.html',
  styleUrls: ['./contributors.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ContributorsComponent implements OnInit, OnDestroy {
  private readonly destroyer$ = new Subject<void>();
  private getContributorsSubscription: Subscription;
  private sorting: MatSort;
  private paging: MatPaginator;

  public dataSource: MatTableDataSource<Contributor>;
  public readonly displayedColumns: string[] = [
    'name',
    'description',
    'type',
    'company',
    'contacts',
    'contribution',
  ];
  public readonly isLoading$ = new BehaviorSubject(true);
  public contributorsList: Contributor[];

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

  constructor(
    private readonly adminApiService: AdminApiService,
  ) {}

  ngOnInit(): void {
    this.getContributors();
  }

  public ngOnDestroy(): void {
    this.destroyer$.next();
    this.destroyer$.complete();
  }

  private getContributors(): void {
    this.getContributorsSubscription?.unsubscribe();
    this.isLoading$.next(true);

    this.getContributorsSubscription = this.adminApiService
      .getContributors()
      .pipe(
        finalize(() => this.isLoading$.next(false)),
        takeUntil(this.destroyer$)
      )
      .subscribe((contributorsList) => {
        this.contributorsList = contributorsList;
        this.dataSource = new MatTableDataSource(contributorsList);
      });
  }

  public applyFilter(event: Event): void {
    const filterValue = (event.target as HTMLInputElement).value;
    this.dataSource.filter = filterValue.trim().toLowerCase();
  }

  // will be implemented in the next story
  public navigateToSelectedContributor(user: Contributor): void {
    console.log(user);
  }
}
