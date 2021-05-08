import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { AdminComponent } from './admin.component';
import { HomeComponent } from './components/home/home.component';
import { LoadFileComponent } from './components/load-file/load-file.component';
import { LoadTasksComponent } from './components/load-tasks/load-tasks.component';
import { AdminGuardService } from './services/admin-guard/admin-guard.service';
import { ExercisesComponent } from './components/exercises/exercises.component';

const routes: Routes = [
  {
    path: '',
    component: AdminComponent,
    canActivate: [AdminGuardService],
    data: {
      animation: 'Admin',
    },
    children: [
      // TODO: will be implement in next tasks
      // {path: 'users', component: UsersComponent},
       {
         path: 'exercises',
         component: ExercisesComponent
       },
      // {path: 'resources', component: ResourcesComponent},
      // {path: 'upload', component: UploadComponent}, //see previous HomeComponent
      {
        path: 'home',
        component: HomeComponent,
        children: [
          {
            path: 'file',
            component: LoadFileComponent,
            data: {
              animation: 'LoadAll',
            },
          },
          {
            path: 'tasks',
            component: LoadTasksComponent,
            data: {
              animation: 'LoadTasks',
            },
          },
          {
            path: '',
            redirectTo: 'file',
            pathMatch: 'full',
          },
        ],
        data: {
          animation: 'Admin',
        },
      },
      {
        path: 'statistics',
        loadChildren: () => import('./modules/statistics/statistics.module').then((m) => m.StatisticsModule),
      },
      {
        path: '',
        redirectTo: 'home',
        pathMatch: 'full',
      },
    ],
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
  providers: [AdminGuardService],
})
export class AdminRoutingModule {}
