import { Routes } from '@angular/router';

import { AuthAccessGuard } from '@root/guards/auth-access.guard';
import { GuestAccessGuard } from '@root/guards/guest-access.guard';

export const APP_ROUTES: Routes = [
  {
    path: 'auth',
    canLoad: [GuestAccessGuard],
    canActivate: [GuestAccessGuard],
    // loadComponent: () => import('./modules/auth/auth.component').then((m) => m.AuthComponent),
    loadChildren: () => import('./modules/auth/auth.routes')
      .then(m => m.AUTH_ROUTES)
  },
  {
    path: '',
    canLoad: [AuthAccessGuard],
    canActivate: [AuthAccessGuard],
    // loadComponent: () => import('./modules/admin/admin.component').then((m) => m.AdminComponent),
    loadChildren: () => import('./modules/admin/admin.routes')
      .then(m => m.ADMIN_ROUTES)
  },
  {
    path: '**',
    loadComponent: () => import('@root/components/not-found/not-found.component').then(m => m.NotFoundComponent),
  },
];
