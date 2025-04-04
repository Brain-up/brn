import { Resources } from '@admin/models/resources.model';
import { ResourcesApiService } from '@admin/services/resources.api.service';
import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject, OnDestroy, OnInit, ViewChild } from '@angular/core';
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
import { BehaviorSubject, finalize, Subject, Subscription, takeUntil } from 'rxjs';

@Component({
  selector: 'app-resources',
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
  templateUrl: './resources.component.html',
  styleUrl: './resources.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ResourcesComponent implements OnInit, OnDestroy {

    private activatedRoute = inject(ActivatedRoute);
    private readonly resourcesApiService = inject(ResourcesApiService);
    private router = inject(Router);
  
    private readonly destroyer$ = new Subject<void>();
    private getResourcesSubscription: Subscription;
    private sorting: MatSort;
    private paging: MatPaginator;
  
    public dataSource: MatTableDataSource<Resources>;
    public readonly displayedColumns: string[] = [
      'columnNumber',
      'description',
      'audioFileUrl',
      'soundsCount',
      'word',
      'wordType',
      'wordPronounce',
      'pictureFileUrl'
    ];
    public readonly isLoading$ = new BehaviorSubject(true);
    public resourcesList: Resources[];
    
  
    // TODO: Skipped for migration because:
    //  Accessor queries cannot be migrated as they are too complex.
    @ViewChild(MatSort) set sort(sort: MatSort) {
      this.sorting = sort;
      if (this.sorting) {
        this.dataSource.sort = this.sorting;
      }
    }

    ngOnInit(): void {
      this.router.routeReuseStrategy.shouldReuseRoute = () => false;
      this.getResources();
    }

    ngOnDestroy(): void {
      this.destroyer$.next();
      this.destroyer$.complete();
    }

   private getResources(): void {
      this.getResourcesSubscription?.unsubscribe();
      this.isLoading$.next(true);
  
      this.getResourcesSubscription = this.resourcesApiService
        .getResources()
        .pipe(
          finalize(() => this.isLoading$.next(false)),
          takeUntil(this.destroyer$)
        )
        .subscribe((resourcesList) => {
          this.resourcesList = resourcesList;
          this.dataSource = new MatTableDataSource(resourcesList);
        });
    }
  
    public applyFilter(event: Event): void {
      const filterValue = (event.target as HTMLInputElement).value;
      this.dataSource.filter = filterValue.trim().toLowerCase();
    }
  
    public navigateToSelectedResource(resource: Resources): void {
      this.router.navigate(['resource', resource.id], {
        relativeTo: this.activatedRoute,
        state: { data: resource },
      });
    }
  
    public addResources(): void {
      this.router.navigate(['resource'], {
        relativeTo: this.activatedRoute,
      });
    }


}
