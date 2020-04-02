import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import { EffectsModule } from '@ngrx/effects';

import { UploadFileComponent } from './components/upload-file/upload-file.component';
import { SnackBarService } from './services/snack-bar/snack-bar.service';

@NgModule({
  declarations: [UploadFileComponent],
  exports: [UploadFileComponent],
  providers: [SnackBarService],
  imports: [
    CommonModule,
  ]
})
export class SharedModule {
}
