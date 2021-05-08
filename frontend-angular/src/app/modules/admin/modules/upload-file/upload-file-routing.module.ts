import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { UploadFileComponent } from './upload-file.component';

const routes: Routes = [
  {
    path: '',
    component: UploadFileComponent,
    children: [
      {
        path: 'file',
        loadChildren: () => import('./modules/load-file/load-file.module').then((m) => m.LoadFileModule),
      },
      {
        path: 'tasks',
        loadChildren: () => import('./modules/load-tasks/load-tasks.module').then((m) => m.LoadTasksModule),
      },
      {
        path: '',
        redirectTo: 'file',
        pathMatch: 'full',
      },
    ],
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class UploadFileRoutingModule {}
