import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {UploadFileComponent} from './upload-file.component';
import {UploadService} from './service/upload.service';

@NgModule({
  declarations: [UploadFileComponent],
  exports: [UploadFileComponent],
  providers: [UploadService],
  imports: [
    CommonModule
  ]
})
export class UploadFileModule {
}
