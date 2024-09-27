import { ActivatedRoute, Router } from '@angular/router';
import { ChangeDetectionStrategy, Component, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { BehaviorSubject, Subject, Subscription } from 'rxjs';
import { MatSort } from '@angular/material/sort';
import { MatPaginator } from '@angular/material/paginator';
import { MatTableDataSource } from '@angular/material/table';
import { finalize, takeUntil } from 'rxjs/operators';
import { Contributor } from '@admin/models/contrubutor.model';
import { ContributorApiService } from '@admin/services/api/contributor-api.service';

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
    'active'
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
    private activatedRoute: ActivatedRoute,
    private readonly contributorApiService: ContributorApiService,
    private router: Router,
  ) {
  }

  ngOnInit(): void {
    this.router.routeReuseStrategy.shouldReuseRoute = () => false;
    this.getContributors();
  }

  public ngOnDestroy(): void {
    this.destroyer$.next();
    this.destroyer$.complete();
  }

  private getContributors(): void {
    this.getContributorsSubscription?.unsubscribe();
    this.isLoading$.next(true);

    this.getContributorsSubscription = this.contributorApiService
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

  public navigateToSelectedContributor(contributor: Contributor): void {
    this.router.navigate(['contributor', contributor.id], {
      relativeTo: this.activatedRoute,
      state: {data: contributor},
    });
  }

  public addContributor(): void {
    this.router.navigate(['contributor'], {
      relativeTo: this.activatedRoute,
    });
  }
}
