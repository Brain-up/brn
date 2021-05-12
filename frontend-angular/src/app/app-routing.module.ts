import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AuthAccessGuard } from '@root/guards/auth-access.guard';
import { GuestAccessGuard } from '@root/guards/guest-access.guard';

const routes: Routes = [
  {
    path: 'auth',
    canLoad: [GuestAccessGuard],
    canActivate: [GuestAccessGuard],
    loadChildren: () => import('./modules/auth/auth.module').then((m) => m.AuthModule),
  },
  {
    path: '',
    canLoad: [AuthAccessGuard],
    canActivate: [AuthAccessGuard],
    loadChildren: () => import('./modules/admin/admin.module').then((m) => m.AdminModule),
  },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule],
})
export class AppRoutingModule {}
