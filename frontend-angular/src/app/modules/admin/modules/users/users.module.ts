import { AdminApiService } from '@admin/services/api/admin-api.service';
import { AdminApiServiceFake } from '@admin/services/api/admin-api.service.fake';
import { NgModule } from '@angular/core';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { TranslateModule } from '@ngx-translate/core';
import { TogglerModule } from '@shared/components/toggler/toggler.module';
import { UsersRoutingModule } from './users-routing.module';
import { UsersComponent } from './users.component';
import { CommonModule } from '@angular/common';
import { UsersTableComponent } from './components/users-table/users-table.component';
import { BarChartModule } from '@shared/components/bar-chart/bar-chart.module';

@NgModule({
  declarations: [UsersComponent, UsersTableComponent],
  imports: [
    CommonModule,
    UsersRoutingModule,
    TranslateModule,
    TogglerModule,
    BarChartModule,
    MatInputModule,
    MatIconModule,
  ],
  providers: [{ provide: AdminApiService, useFactory: () => new AdminApiServiceFake({ responseDelayInMs: 0 }) }],
})
export class UsersModule {}
