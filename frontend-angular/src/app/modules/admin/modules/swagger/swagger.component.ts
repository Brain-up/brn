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
  private readonly destroyer$ = new Subject<void>();
  constructor(private readonly adminApiService: AdminApiService) {}

  ngOnInit(): void {
    this.adminApiService
      .getSwaggerUi()
      .pipe(takeUntil(this.destroyer$))
      .subscribe((swagger) => {
        this.swagger = swagger;
      });
  }

  _swagger: any = undefined;

  set swagger(value: string) {
    this._swagger = SwaggerUI({
      docExpansion: 'none',
      domNode: document.getElementById('swagger-ui-item'),
      layout: 'BaseLayout',
      spec: JSON.parse(value),
    });
  }
  get swagger() {
    return this._swagger;
  }

  public ngOnDestroy(): void {
    this.destroyer$.next();
    this.destroyer$.complete();
    // have no idea how to destroy swagger-ui
  }
}
