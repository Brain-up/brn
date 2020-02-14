import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import { EffectsModule } from '@ngrx/effects';

import { UploadFileComponent } from './components/upload-file/upload-file.component';
import { UploadService } from './services/upload/upload.service';
import { UploadEffects } from '../admin/ngrx/effects';
import { SnackBarService } from './services/snack-bar/snack-bar.service';

@NgModule({
  declarations: [UploadFileComponent],
  exports: [UploadFileComponent],
  providers: [UploadService, SnackBarService],
  imports: [
    CommonModule,
    EffectsModule.forFeature([UploadEffects]),
  ]
})
export class SharedModule {
}
