import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LoginComponent } from './components/login/login.component';
import { StoreModule } from '@ngrx/store';
import * as fromAuthNgrx from './ngrx/reducers';
import { HomeComponent } from './components/home/home.component';
import { MatToolbarModule, MatSidenavModule } from '@angular/material';
import { RouterModule, Routes } from '@angular/router';
import { SessionService } from './services/session/session.service';
import { EffectsModule } from '@ngrx/effects';
import { AuthEffects } from './ngrx/effects';
import { ReactiveFormsModule } from '@angular/forms';

const authRoutes: Routes = [
  {
    path: 'auth',
    component: HomeComponent,
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
]
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
    SessionService
  ]
})
export class AuthModule { }
