import { Contributor } from '@admin/models/contrubutor.model';
import { ContributorApiService } from '@admin/services/api/contributor-api.service';
import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, OnDestroy, OnInit, ViewChild, inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatRippleModule } from '@angular/material/core';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginator, MatPaginatorModule } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSort, MatSortModule } from '@angular/material/sort';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { BehaviorSubject, Subject, Subscription } from 'rxjs';
import { finalize, takeUntil } from 'rxjs/operators';

@Component({
  selector: 'app-contributors',
  templateUrl: './contributors.component.html',
  styleUrls: ['./contributors.component.scss'],
  imports: [
    CommonModule,
    MatProgressBarModule,
    TranslateModule,
    MatPaginatorModule,
    MatIconModule,
    MatInputModule,
    MatButtonModule,
    MatTableModule,
    MatRippleModule,
    MatSortModule,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ContributorsComponent implements OnInit, OnDestroy {
  private activatedRoute = inject(ActivatedRoute);
  private readonly contributorApiService = inject(ContributorApiService);
  private router = inject(Router);

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
      state: { data: contributor },
    });
  }

  public addContributor(): void {
    this.router.navigate(['contributor'], {
      relativeTo: this.activatedRoute,
    });
  }
}
