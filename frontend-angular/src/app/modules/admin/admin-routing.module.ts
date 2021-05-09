import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { AdminComponent } from './admin.component';

const routes: Routes = [
  {
    path: '',
    component: AdminComponent,
    children: [
      {
        path: '',
        pathMatch: 'full',
        redirectTo: 'upload-file',
      },
      {
        path: 'upload-file',
        loadChildren: () => import('./modules/upload-file/upload-file.module').then((m) => m.UploadFileModule),
      },
      {
        path: 'statistics',
        loadChildren: () => import('./modules/statistics/statistics.module').then((m) => m.StatisticsModule),
      },
      {
        path: 'exercises',
        loadChildren: () => import('./modules/exercises/exercises.module').then((m) => m.ExercisesModule),
      },
    ],
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class AdminRoutingModule {}
