import { Routes } from '@angular/router';


export const STATISTICS_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./statistics.component').then(m => m.StatisticsComponent),
  },
];

