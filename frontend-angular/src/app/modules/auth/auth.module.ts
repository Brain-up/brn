import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LoginComponent } from './components/login/login.component';
import { StoreModule } from '@ngrx/store';
import * as fromAuthNgrx from './ngrx/reducers';
import { HomeComponent } from './components/home/home.component';
import {MatSidenavModule} from '@angular/material/sidenav';
import {MatToolbarModule} from '@angular/material/toolbar';
import { RouterModule, Routes } from '@angular/router';
import { SessionService } from './services/session/session.service';
import { EffectsModule } from '@ngrx/effects';
import { AuthEffects } from './ngrx/effects';
import { ReactiveFormsModule } from '@angular/forms';
import { AuthGuardService } from './services/auth-guard/auth-guard.service';

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
export class AuthModule { }
