import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {AdminPageComponent} from './admin-page.component';
import {MatButtonModule, MatIconModule, MatSnackBarModule} from '@angular/material';
import {RouterModule} from '@angular/router';
import {LoadFileComponent} from './components/load-file/load-file.component';
import {LoadTasksComponent} from './components/load-tasks/load-tasks.component';
import {ReactiveFormsModule} from '@angular/forms';
import {SharedModule} from '../shared/shared.module';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatSelectModule} from '@angular/material/select';
import {HttpClientModule} from '@angular/common/http';
import {HomeComponent} from './components/home/home.component';
import {MatSidenavModule} from '@angular/material/sidenav';
import {MatToolbarModule} from '@angular/material/toolbar';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { FolderService } from './services/folders/folder.service';
import { UploadService } from './services/upload/upload.service';
import { FormatService } from './services/format/format.service';

@NgModule({
  declarations: [
    AdminPageComponent,
    LoadFileComponent,
    LoadTasksComponent,
    HomeComponent
  ],
  exports: [AdminPageComponent],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    BrowserAnimationsModule,
    HttpClientModule,
    RouterModule.forChild([
      {
        path: '',
        component: AdminPageComponent,
        data: {
          animation: 'Admin'
        },
        children: [
          {
            path: 'home',
            component: HomeComponent,
            children: [
              {
                path: 'file',
                component: LoadFileComponent,
                data: {
                  animation: 'LoadAll'
                }
              },
              {
                path: 'tasks',
                component: LoadTasksComponent,
                data: {
                  animation: 'LoadTasks'
                }
              },
            ],
            data: {
              animation: 'Admin'
            }
          },
          {
            path: '',
            redirectTo: '/home',
            pathMatch: 'full'
          }
        ]
      },

    ]),
    MatButtonModule,
    SharedModule,
    MatFormFieldModule,
    MatSelectModule,
    MatSnackBarModule,
    MatIconModule,
    MatSidenavModule,
    MatToolbarModule,
  ],
  providers: [
    FolderService,
    FormatService,
    UploadService
  ]
})
export class AdminModule {
}
