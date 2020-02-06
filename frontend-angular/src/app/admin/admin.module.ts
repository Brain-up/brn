import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {AdminPageComponent} from './admin-page.component';
import {MatButtonModule, MatIconModule, MatSnackBarModule} from '@angular/material';
import {RouterModule} from '@angular/router';
import {LoadFileComponent} from './components/load-file/load-file.component';
import {LoadTasksComponent} from './components/load-tasks/load-tasks.component';
import {LOAD_FILE_PATH, LOAD_TASKS_FILE} from '../shared/app-path';
import {ReactiveFormsModule} from '@angular/forms';
import {SharedModule} from '../shared/shared.module';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatSelectModule} from '@angular/material/select';
import {HttpClientModule} from '@angular/common/http';
import {HomeComponent} from './components/home/home.component';

@NgModule({
  declarations: [AdminPageComponent, LoadFileComponent, LoadTasksComponent, HomeComponent],
  exports: [AdminPageComponent],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    HttpClientModule,
    RouterModule.forChild([
      {
        path: '',
        component: HomeComponent,
        data: {
          animation: 'Admin'
        }
      },
      {
        path: LOAD_FILE_PATH,
        component: LoadFileComponent,
        data: {
          animation: 'LoadAll'
        }
      },
      {
        path: LOAD_TASKS_FILE,
        component: LoadTasksComponent,
        data: {
          animation: 'LoadTasks'
        }
      }
    ]),
    MatButtonModule,
    SharedModule,
    MatFormFieldModule,
    MatSelectModule,
    MatSnackBarModule,
    MatIconModule
  ]
})
export class AdminModule {
}
