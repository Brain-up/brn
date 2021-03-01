import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { ReactiveFormsModule } from '@angular/forms';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatToolbarModule } from '@angular/material/toolbar';

import { StoreModule } from '@ngrx/store';
import { EffectsModule } from '@ngrx/effects';

import { LoginComponent } from './components/login/login.component';
import { HomeComponent } from './components/home/home.component';
import { SessionService } from './services/session/session.service';
import { AuthGuardService } from './services/auth-guard/auth-guard.service';
import { SessionTokenService } from './services/session/session-token.service';
import { SessionTokenCoderService } from './services/session/session-token-coder.service';
import { SessionTokenMapperService } from './services/session/session-token-mapper.service';
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
    AuthGuardService,
    SessionTokenService,
    SessionTokenCoderService,
    SessionTokenMapperService
  ]
})
export class AuthModule {
}
