import { NgModule } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { TranslateModule } from '@ngx-translate/core';
import { UploadFileRoutingModule } from './upload-file-routing.module';
import { UploadFileComponent } from './upload-file.component';

@NgModule({
  declarations: [UploadFileComponent],
  imports: [UploadFileRoutingModule, TranslateModule, MatButtonModule],
})
export class UploadFileModule {}
