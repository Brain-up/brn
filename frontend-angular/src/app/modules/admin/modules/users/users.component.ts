import { AdminApiService } from '@admin/services/api/admin-api.service';
import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { Subject } from 'rxjs';

@Component({
  selector: 'app-users',
  templateUrl: './users.component.html',
  styleUrls: ['./users.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UsersComponent implements OnInit, OnDestroy {
  private readonly destroyer$ = new Subject<void>();

  constructor(private readonly cdr: ChangeDetectorRef, private readonly adminApiService: AdminApiService) {}

  ngOnInit(): void {}

  ngOnDestroy(): void {
    this.destroyer$.next();
    this.destroyer$.complete();
  }
}
