import { NgModule } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { UploadFileRoutingModule } from './upload-file-routing.module';
import { UploadFileComponent } from './upload-file.component';

@NgModule({
  declarations: [UploadFileComponent],
  imports: [UploadFileRoutingModule, MatButtonModule],
})
export class UploadFileModule {}
