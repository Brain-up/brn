import SwaggerUI from 'swagger-ui';
import { AdminApiService } from '@admin/services/api/admin-api.service';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import {
  Component,
  OnInit,
  ChangeDetectionStrategy,
  AfterViewInit,
  OnDestroy,
} from '@angular/core';

@Component({
  selector: 'app-swagger',
  templateUrl: './swagger.component.html',
  styleUrls: ['./swagger.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SwaggerComponent implements AfterViewInit, OnDestroy, OnInit {
  private readonly destroyer$ = new Subject<void>();
  public swagger;

  constructor(private readonly adminApiService: AdminApiService) {}

  async ngOnInit(): Promise<void> {
    await this.adminApiService
      .getSwaggerUi()
      .pipe(takeUntil(this.destroyer$))
      .subscribe((swagger) => {
        this.swagger = swagger;
      }).toPromise();
  }

  // Use adminApiService, SwaggerUI would GET without the Auth token
  ngAfterViewInit(): void {
    SwaggerUI({
      docExpansion: 'none',
      domNode: document.getElementById('swagger-ui-item'),
      layout: 'BaseLayout',
      spec: JSON.parse(this.swagger),
    });
  }

  public ngOnDestroy(): void {
    this.destroyer$.next();
    this.destroyer$.complete();
  }
}
