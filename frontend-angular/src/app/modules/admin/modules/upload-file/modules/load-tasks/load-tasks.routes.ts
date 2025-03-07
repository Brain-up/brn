import { Routes } from '@angular/router';


export const LOAD_TASKS_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./load-tasks.component').then(m => m.LoadTasksComponent),
  },
];
