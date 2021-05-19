import { NgModule } from '@angular/core';
import { LoginComponent } from './login.component';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { LoginRoutingModule } from './login-routing.module';
import { TranslateModule } from '@ngx-translate/core';

@NgModule({
  declarations: [LoginComponent],
  imports: [CommonModule, ReactiveFormsModule, LoginRoutingModule, TranslateModule],
})
export class LoginModule {}
