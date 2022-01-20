import { AdminComponent } from './admin.component';
import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

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
        loadChildren: () =>
          import('./modules/users/users.module').then((m) => m.UsersModule),
      },
      {
        path: 'exercises',
        loadChildren: () =>
          import('./modules/exercises/exercises.module').then(
            (m) => m.ExercisesModule,
          ),
      },
      {
        path: 'profile',
        loadChildren: () =>
          import('./modules/profile/profile.module').then(
            (m) => m.ProfileModule,
          ),
      },
      {
        path: 'upload-file',
        loadChildren: () =>
          import('./modules/upload-file/upload-file.module').then(
            (m) => m.UploadFileModule,
          ),
      },
      {
        path: 'swagger',
        loadChildren: () =>
          import('./modules/swagger/swagger.module').then(
            (m) => m.SwaggerModule,
          ),
      },
    ],
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class AdminRoutingModule {}
