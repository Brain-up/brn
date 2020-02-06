import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';

import { UploadFileComponent } from './components/upload-file/upload-file.component';
import { UploadService } from './services/upload/upload.service';

@NgModule({
  declarations: [UploadFileComponent],
  exports: [UploadFileComponent],
  providers: [UploadService],
  imports: [
    CommonModule
  ]
})
export class SharedModule {
}
