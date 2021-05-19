import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { MatSelectModule } from '@angular/material/select';
import { TranslateModule } from '@ngx-translate/core';
import { UploadFileInputModule } from '@shared/components/upload-file-input/upload-file-input.module';
import { LoadTasksRoutingModule } from './load-tasks-routing.module';
import { LoadTasksComponent } from './load-tasks.component';

@NgModule({
  declarations: [LoadTasksComponent],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    LoadTasksRoutingModule,
    TranslateModule,
    UploadFileInputModule,
    MatSelectModule,
  ],
})
export class LoadTasksModule {}
