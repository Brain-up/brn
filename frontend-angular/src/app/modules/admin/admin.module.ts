import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { AdminComponent } from './admin.component';
import { AdminRoutingModule } from './admin-routing.module';
import { AdminApiService } from './services/api/admin-api.service';
import { CloudApiService } from './services/api/cloud-api.service';
import { GroupApiService } from './services/api/group-api.service';
import { SeriesApiService } from './services/api/series-api.service';
import { SubGroupApiService } from './services/api/sub-group-api.service';
import { TranslateModule } from '@ngx-translate/core';

@NgModule({
  declarations: [AdminComponent],
  imports: [CommonModule, AdminRoutingModule, TranslateModule, MatButtonModule, MatToolbarModule],
  providers: [AdminApiService, CloudApiService, GroupApiService, SeriesApiService, SubGroupApiService],
})
export class AdminModule {}
