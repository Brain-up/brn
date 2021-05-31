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
        redirectTo: 'users',
      },
      {
        path: 'users',
        loadChildren: () => import('./modules/users/users.module').then((m) => m.UsersModule),
      },
      {
        path: 'exercises',
        loadChildren: () => import('./modules/exercises/exercises.module').then((m) => m.ExercisesModule),
      },
      {
        path: 'upload-file',
        loadChildren: () => import('./modules/upload-file/upload-file.module').then((m) => m.UploadFileModule),
      },
    ],
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class AdminRoutingModule {}
