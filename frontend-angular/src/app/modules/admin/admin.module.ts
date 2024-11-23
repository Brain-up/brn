import { AdminApiService } from './services/api/admin-api.service';
import { AdminComponent } from './admin.component';
import { AdminRoutingModule } from './admin-routing.module';
import { AuthenticationApiService } from '@auth/services/api/authentication-api.service';
import { CloudApiService } from './services/api/cloud-api.service';
import { CommonModule } from '@angular/common';
import { GroupApiService } from './services/api/group-api.service';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatToolbarModule } from '@angular/material/toolbar';
import { NgModule } from '@angular/core';

import { SeriesApiService } from './services/api/series-api.service';
import { SubGroupApiService } from './services/api/sub-group-api.service';
import { TranslateModule } from '@ngx-translate/core';

@NgModule({
    imports: [
    AdminRoutingModule,
    CommonModule,
    MatButtonModule,
    MatIconModule,
    MatToolbarModule,
    TranslateModule,
    AdminComponent,
],
    providers: [
        AdminApiService,
        AuthenticationApiService,
        CloudApiService,
        GroupApiService,
        SeriesApiService,
        SubGroupApiService,
    ],
})
export class AdminModule {}
