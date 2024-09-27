import { CommonModule } from '@angular/common';
import { MatLegacyCardModule as MatCardModule } from '@angular/material/legacy-card';
import { MatLegacyFormFieldModule as MatFormFieldModule } from '@angular/material/legacy-form-field';
import { MatLegacyInputModule as MatInputModule } from '@angular/material/legacy-input';
import { NgModule } from '@angular/core';
import { PipesModule } from '@shared/pipes/pipes.module';
import { ProfileComponent } from './profile.component';
import { ProfileRoutingModule } from './profile-routing.module';
import { TranslateModule } from '@ngx-translate/core';

@NgModule({
  declarations: [ProfileComponent],
  imports: [
    CommonModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    PipesModule,
    ProfileRoutingModule,
    TranslateModule
  ],
})
export class ProfileModule {}
