import { NgModule } from '@angular/core';
import { MatToolbarModule } from '@angular/material/toolbar';
import { TranslateModule } from '@ngx-translate/core';
import { AuthRoutingModule } from './auth-routing.module';
import { AuthComponent } from './auth.component';
import { AuthenticationApiService } from './services/api/authentication-api.service';

@NgModule({
    imports: [AuthRoutingModule, TranslateModule, MatToolbarModule, AuthComponent],
    providers: [AuthenticationApiService],
})
export class AuthModule {}
