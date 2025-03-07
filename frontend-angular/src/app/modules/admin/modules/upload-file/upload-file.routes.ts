import { Routes } from '@angular/router';
import { UploadFileComponent } from './upload-file.component';

export const UPLOAD_FILE_ROUTES: Routes = [
  {
    path: '',
    component: UploadFileComponent,
    children: [
      {
        path: '',
        pathMatch: 'full',
        redirectTo: 'files',
      },
      {
        path: 'files',
        loadComponent: () => import('./modules/load-files/load-files.component').then((m) => m.LoadFilesComponent),
        loadChildren: () => import('./modules/load-files/load-files.routes').then((m) => m.LOAD_FILES_ROUTES)
      },
      {
        path: 'tasks',
        loadComponent: () => import('./modules/load-tasks/load-tasks.component').then((m) => m.LoadTasksComponent),
        loadChildren: () => import('./modules/load-tasks/load-tasks.routes').then(m => m.LOAD_TASKS_ROUTES)
      },
    ],
  },
];
