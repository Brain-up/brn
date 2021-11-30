import { AdminApiService } from '@admin/services/api/admin-api.service';
import { NgModule } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { TranslateModule } from '@ngx-translate/core';
import { TogglerModule } from '@shared/components/toggler/toggler.module';
import { UsersRoutingModule } from './users-routing.module';
import { UsersComponent } from './users.component';
import { CommonModule } from '@angular/common';
import { UsersTableComponent } from './components/users-table/users-table.component';
import { BarChartModule } from '@shared/components/bar-chart/bar-chart.module';
import { PaginatorModule } from '@shared/components/paginator/paginator.module';
import { SearchInputModule } from '@shared/components/search-input/search-input.module';
import { MatProgressBarModule } from '@angular/material/progress-bar';

@NgModule({
  declarations: [UsersComponent, UsersTableComponent],
  imports: [
    CommonModule,
    UsersRoutingModule,
    TranslateModule,
    SearchInputModule,
    TogglerModule,
    BarChartModule,
    PaginatorModule,
    MatIconModule,
    MatProgressBarModule,
  ],
  providers: [AdminApiService],
})
export class UsersModule {}
