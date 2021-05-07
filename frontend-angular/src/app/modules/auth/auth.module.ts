import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatToolbarModule } from '@angular/material/toolbar';
import { StoreModule } from '@ngrx/store';
import { EffectsModule } from '@ngrx/effects';
import { LoginComponent } from './components/login/login.component';
import { SessionService } from './services/session/session.service';
import { SessionTokenService } from './services/session/session-token.service';
import { SessionTokenCoderService } from './services/session/session-token-coder.service';
import { SessionTokenMapperService } from './services/session/session-token-mapper.service';
import * as fromAuthNgrx from './ngrx/reducers';
import { AuthEffects } from './ngrx/effects';
import { AuthRoutingModule } from './auth-routing.module';
import { AuthComponent } from './auth.component';

@NgModule({
  declarations: [AuthComponent, LoginComponent],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    AuthRoutingModule,
    MatSidenavModule,
    MatToolbarModule,
    StoreModule.forFeature(fromAuthNgrx.authFeatureKey, fromAuthNgrx.authReducer),
    EffectsModule.forFeature([AuthEffects]),
  ],
  providers: [SessionService, SessionTokenService, SessionTokenCoderService, SessionTokenMapperService],
})
export class AuthModule {}
