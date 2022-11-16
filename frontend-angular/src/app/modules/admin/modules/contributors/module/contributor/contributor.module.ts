import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ContributorRoutingModule } from './contributor-routing.module';
import { ContributorComponent } from './contributor.component';
import { ReactiveFormsModule } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';
import { MatIconModule } from '@angular/material/icon';


@NgModule({
  declarations: [
    ContributorComponent
  ],
  imports: [
    CommonModule,
    ContributorRoutingModule,
    ReactiveFormsModule,
    TranslateModule,
    MatIconModule
  ]
})
export class ContributorModule { }
