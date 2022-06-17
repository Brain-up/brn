import SwaggerUI from 'swagger-ui';
import { AdminApiService } from '@admin/services/api/admin-api.service';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import {
  Component,
  OnInit,
  ChangeDetectionStrategy,
  OnDestroy,
} from '@angular/core';

@Component({
  selector: 'app-swagger',
  templateUrl: './swagger.component.html',
  styleUrls: ['./swagger.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SwaggerComponent implements OnDestroy, OnInit {
  constructor(private readonly adminApiService: AdminApiService) {}

  set swagger(value: string) {
    this.swaggerUI = SwaggerUI({
      docExpansion: 'none',
      domNode: document.getElementById('swagger-ui-item'),
      layout: 'BaseLayout',
      spec: JSON.parse(value),
    });
  }

  get swagger() {
    return this.swaggerUI;
  }

  private readonly destroyer$ = new Subject<void>();

  swaggerUI: any = undefined;

  ngOnInit(): void {
    this.adminApiService
      .getSwaggerUi()
      .pipe(takeUntil(this.destroyer$))
      .subscribe((swagger) => {
        this.swagger = swagger;
      });
  }

  public ngOnDestroy(): void {
    this.destroyer$.next();
    this.destroyer$.complete();
    // have no idea how to destroy swagger-ui
  }
}
