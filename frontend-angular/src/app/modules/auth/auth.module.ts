import { NgModule } from '@angular/core';
import { MatToolbarModule } from '@angular/material/toolbar';
import { AuthRoutingModule } from './auth-routing.module';
import { AuthComponent } from './auth.component';
import { AuthenticationApiService } from './services/api/authentication-api.service';

@NgModule({
  declarations: [AuthComponent],
  imports: [AuthRoutingModule, MatToolbarModule],
  providers: [AuthenticationApiService],
})
export class AuthModule {}
