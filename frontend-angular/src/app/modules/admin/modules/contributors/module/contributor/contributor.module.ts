import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ContributorRoutingModule } from './contributor-routing.module';
import { ContributorComponent } from './contributor.component';
import { ReactiveFormsModule } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';
import { MatIconModule } from '@angular/material/icon';
import { MatLegacyProgressBarModule as MatProgressBarModule } from '@angular/material/legacy-progress-bar';
import { MatLegacySlideToggleModule as MatSlideToggleModule } from '@angular/material/legacy-slide-toggle';


@NgModule({
  declarations: [
    ContributorComponent
  ],
  imports: [
    CommonModule,
    ContributorRoutingModule,
    ReactiveFormsModule,
    TranslateModule,
    MatIconModule,
    MatProgressBarModule,
    MatSlideToggleModule
  ]
})
export class ContributorModule { }
