import { Routes } from '@angular/router';


export const AUTH_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./auth.component').then(m => m.AuthComponent),
    children: [
      {
        path: '',
        pathMatch: 'full',
        redirectTo: 'login',
      },
      {
        path: 'login',
        loadComponent: () => import('./modules/login/login.component').then((m) => m.LoginComponent),
      },
    ],
  },
];
