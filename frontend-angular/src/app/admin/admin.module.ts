import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {AdminPageComponent} from './admin-page.component';
import {MatButtonModule, MatSnackBarModule} from '@angular/material';
import {RouterModule} from '@angular/router';
import {LoadFileComponent} from './components/load-file/load-file.component';
import {LoadTasksComponent} from './components/load-tasks/load-tasks.component';
import {LOAD_FILE_PATH, LOAD_TASKS_FILE} from '../shared/app-path';
import {ReactiveFormsModule} from '@angular/forms';
import {SharedModule} from '../shared/shared.module';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatSelectModule} from '@angular/material/select';
import {HttpClientModule} from '@angular/common/http';
import {UploadFileModule} from '../shared/upload-file/upload-file.module';

@NgModule({
  declarations: [AdminPageComponent, LoadFileComponent, LoadTasksComponent],
  exports: [AdminPageComponent],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    HttpClientModule,
    RouterModule.forChild([
      {
        path: LOAD_FILE_PATH,
        component: LoadFileComponent
      },
      {
        path: LOAD_TASKS_FILE,
        component: LoadTasksComponent
      }
    ]),
    UploadFileModule,
    MatButtonModule,
    SharedModule,
    MatFormFieldModule,
    MatSelectModule,
    MatSnackBarModule
  ]
})
export class AdminModule {
}
