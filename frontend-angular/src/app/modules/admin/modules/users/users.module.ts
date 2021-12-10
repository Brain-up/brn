import { AdminApiService } from '@admin/services/api/admin-api.service';
import { BarChartModule } from '@shared/components/bar-chart/bar-chart.module';
import { CommonModule } from '@angular/common';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatSortModule } from '@angular/material/sort';
import { MatTableModule } from '@angular/material/table';
import { NgModule } from '@angular/core';
import { PaginatorModule } from '@shared/components/paginator/paginator.module';
import { SearchInputModule } from '@shared/components/search-input/search-input.module';
import { TogglerModule } from '@shared/components/toggler/toggler.module';
import { TranslateModule } from '@ngx-translate/core';
import { UsersComponent } from './users.component';
import { UsersRoutingModule } from './users-routing.module';
import { AdminApiServiceFake } from '@admin/services/api/admin-api.service.fake';

@NgModule({
  declarations: [UsersComponent],
  imports: [
    BarChartModule,
    CommonModule,
    MatButtonToggleModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatPaginatorModule,
    MatProgressBarModule,
    MatSlideToggleModule,
    MatSortModule,
    MatTableModule,
    PaginatorModule,
    SearchInputModule,
    TogglerModule,
    TranslateModule,
    UsersRoutingModule,
  ],
  // providers: [AdminApiService, ],
  providers: [{ provide: AdminApiService, useFactory: () => new AdminApiServiceFake() }],
})
export class UsersModule {}
