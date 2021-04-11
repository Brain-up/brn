import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClientModule } from '@angular/common/http';
import { ReactiveFormsModule } from '@angular/forms';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { MatButtonModule } from '@angular/material/button';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';

import { StoreModule } from '@ngrx/store';
import { EffectsModule } from '@ngrx/effects';
import * as fromAdminNgrx from './ngrx/reducers';
import { AdminEffects } from './ngrx/effects';

import { AdminPageComponent } from './admin-page.component';
import { LoadFileComponent } from './components/load-file/load-file.component';
import { LoadTasksComponent } from './components/load-tasks/load-tasks.component';
import { SharedModule } from '../shared/shared.module';
import { HomeComponent } from './components/home/home.component';
import { FoldersService } from './services/folders/folders.service';
import { UploadService } from './services/upload/upload.service';
import { FormatService } from './services/format/format.service';
import { AdminGuardService } from './services/admin-guard/admin-guard.service';
import { AdminPageRoutingModule } from './admin-page-routing.module';
import { ExercisesComponent } from './components/exercises/exercises.component';
import { SelectPanelComponent } from './components/exercises/select-panel/select-panel.component';

@NgModule({
  declarations: [
    AdminPageComponent,
    LoadFileComponent,
    LoadTasksComponent,
    HomeComponent,
    ExercisesComponent,
    SelectPanelComponent
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
    MatTableModule,
    MatProgressSpinnerModule,
    MatButtonToggleModule,
    MatSlideToggleModule,
    StoreModule.forFeature(fromAdminNgrx.adminFeatureKey, fromAdminNgrx.adminReducer),
    EffectsModule.forFeature([AdminEffects])
  ],
  providers: [
    FoldersService,
    FormatService,
    UploadService,
    AdminGuardService
  ]
})
export class AdminModule {
}
