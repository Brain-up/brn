import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { UploadFileInputModule } from '@shared/components/upload-file-input/upload-file-input.module';
import { LoadFileRoutingModule } from './load-file-routing.module';
import { LoadFileComponent } from './load-file.component';

@NgModule({
  declarations: [LoadFileComponent],
  imports: [CommonModule, ReactiveFormsModule, LoadFileRoutingModule, UploadFileInputModule],
})
export class LoadFileModule {}
