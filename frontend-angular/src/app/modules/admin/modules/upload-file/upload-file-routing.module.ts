import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { UploadFileComponent } from './upload-file.component';

const routes: Routes = [
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
        loadChildren: () => import('./modules/load-files/load-files.module').then((m) => m.LoadFilesModule),
      },
      {
        path: 'tasks',
        loadChildren: () => import('./modules/load-tasks/load-tasks.module').then((m) => m.LoadTasksModule),
      },
    ],
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class UploadFileRoutingModule {}
