import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';
import { UploadFileInputModule } from '@shared/components/upload-file-input/upload-file-input.module';
import { LoadFilesRoutingModule } from './load-files-routing.module';
import { LoadFilesComponent } from './load-files.component';

@NgModule({
    imports: [CommonModule, ReactiveFormsModule, LoadFilesRoutingModule, TranslateModule, UploadFileInputModule, LoadFilesComponent],
})
export class LoadFilesModule {}
