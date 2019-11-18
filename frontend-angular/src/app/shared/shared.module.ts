import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {UploadFileComponent} from './upload-file/upload-file.component';


@NgModule({
  declarations: [UploadFileComponent],
  exports: [UploadFileComponent],
  imports: [
    CommonModule
  ]
})
export class SharedModule {
}
