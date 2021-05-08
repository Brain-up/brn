import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatToolbarModule } from '@angular/material/toolbar';
import { AdminComponent } from './admin.component';
import { AdminRoutingModule } from './admin-routing.module';
import { AdminApiService } from './services/api/admin-api.service';
import { CloudApiService } from './services/api/cloud-api.service';
import { GroupApiService } from './services/api/group-api.service';
import { SeriesApiService } from './services/api/series-api.service';
import { MatButtonModule } from '@angular/material/button';

@NgModule({
  declarations: [AdminComponent],
  imports: [CommonModule, AdminRoutingModule, MatButtonModule, MatToolbarModule],
  providers: [AdminApiService, CloudApiService, GroupApiService, SeriesApiService],
})
export class AdminModule {}
