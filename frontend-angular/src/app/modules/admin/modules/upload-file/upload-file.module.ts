import { NgModule } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { TranslateModule } from '@ngx-translate/core';
import { UploadFileRoutingModule } from './upload-file-routing.module';
import { UploadFileComponent } from './upload-file.component';

@NgModule({
    imports: [UploadFileRoutingModule, TranslateModule, MatButtonModule, UploadFileComponent],
})
export class UploadFileModule {}
