import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { ReactiveFormsModule } from '@angular/forms';
import { MatToolbarModule, MatSidenavModule } from '@angular/material';

import { StoreModule } from '@ngrx/store';
import { EffectsModule } from '@ngrx/effects';

import { LoginComponent } from './components/login/login.component';
import { HomeComponent } from './components/home/home.component';
import { SessionService } from './services/session/session.service';
import { AuthGuardService } from './services/auth-guard/auth-guard.service';
import * as fromAuthNgrx from './ngrx/reducers';
import { AuthEffects } from './ngrx/effects';

const authRoutes: Routes = [
  {
    path: 'auth',
    component: HomeComponent,
    canActivate: [AuthGuardService],
    children: [
      {
        path: 'login',
        component: LoginComponent
      }, {
        path: '',
        redirectTo: '/auth/login',
        pathMatch: 'full'
      }
    ]
  }
];

@NgModule({
  declarations: [
    LoginComponent,
    HomeComponent
  ],
  imports: [
    CommonModule,
    MatSidenavModule,
    MatToolbarModule,
    ReactiveFormsModule,
    RouterModule.forChild(authRoutes),
    StoreModule.forFeature(fromAuthNgrx.authFeatureKey, fromAuthNgrx.authReducer),
    EffectsModule.forFeature([AuthEffects])
  ],
  providers: [
    SessionService,
    AuthGuardService
  ]
})
export class AuthModule {
}
