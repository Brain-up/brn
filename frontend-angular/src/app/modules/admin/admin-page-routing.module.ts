import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';

import { AdminPageComponent } from './admin-page.component';
import { HomeComponent } from './components/home/home.component';
import { LoadFileComponent } from './components/load-file/load-file.component';
import { LoadTasksComponent } from './components/load-tasks/load-tasks.component';
import { AdminGuardService } from './services/admin-guard/admin-guard.service';

const adminRoutes: Routes = [
  {
    path: 'admin',
    component: AdminPageComponent,
    canActivate: [AdminGuardService],
    data: {
      animation: 'Admin'
    },
    children: [
      // TODO: will be implement in next tasks
      // {path: 'users', component: UsersComponent},
      // {path: 'exercises', component: ExercisesComponent},
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
        path: 'statistics',
        loadChildren: () => import('./modules/statistics/statistics.module').then(m => m.StatisticsModule)
      },
      {
        path: '',
        redirectTo: '/admin',
        pathMatch: 'full'
      }
    ]
  },
];

@NgModule({
  imports: [
    RouterModule.forChild(adminRoutes)
  ],
  exports: [
    RouterModule
  ]
})
export class AdminPageRoutingModule {
}
