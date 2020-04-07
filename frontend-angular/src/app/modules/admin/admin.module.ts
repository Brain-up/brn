import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {AdminPageComponent} from './admin-page.component';
import {MatButtonModule, MatIconModule, MatSnackBarModule} from '@angular/material';
import {RouterModule} from '@angular/router';
import {LoadFileComponent} from './components/load-file/load-file.component';
import {LoadTasksComponent} from './components/load-tasks/load-tasks.component';
import {ReactiveFormsModule} from '@angular/forms';
import {SharedModule} from '../shared/shared.module';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatSelectModule} from '@angular/material/select';
import {HttpClientModule} from '@angular/common/http';
import {HomeComponent} from './components/home/home.component';
import {MatSidenavModule} from '@angular/material/sidenav';
import {MatToolbarModule} from '@angular/material/toolbar';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { FolderService } from './services/folders/folder.service';
import { UploadService } from './services/upload/upload.service';
import { FormatService } from './services/format/format.service';
import { StoreModule } from '@ngrx/store';
import * as fromAdminNgrx from './ngrx/reducers';
import { AdminGuardService } from './services/admin-guard/admin-guard.service';
import { AdminPageRoutingModule } from './admin-page-routing.module';
import { EffectsModule } from '@ngrx/effects';
import { AdminEffects } from './ngrx/effects';


@NgModule({
  declarations: [
    AdminPageComponent,
    LoadFileComponent,
    LoadTasksComponent,
    HomeComponent,
  ],
  exports: [AdminPageComponent],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    BrowserAnimationsModule,
    HttpClientModule,
    AdminPageRoutingModule,
    MatButtonModule,
    SharedModule,
    MatFormFieldModule,
    MatSelectModule,
    MatSnackBarModule,
    MatIconModule,
    MatSidenavModule,
    MatToolbarModule,
    StoreModule.forFeature(fromAdminNgrx.adminFeatureKey, fromAdminNgrx.adminReducer),
    EffectsModule.forFeature([AdminEffects])
  ],
  providers: [
    FolderService,
    FormatService,
    UploadService,
    AdminGuardService
  ]
})
export class AdminModule {
}
