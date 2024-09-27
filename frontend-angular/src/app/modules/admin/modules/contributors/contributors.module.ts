import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ContributorsRoutingModule } from './contributors-routing.module';
import { ContributorsComponent } from './contributors.component';
import { MatLegacyProgressBarModule as MatProgressBarModule } from '@angular/material/legacy-progress-bar';
import { TranslateModule } from '@ngx-translate/core';
import { MatLegacyPaginatorModule as MatPaginatorModule } from '@angular/material/legacy-paginator';
import { MatIconModule } from '@angular/material/icon';
import { MatLegacyInputModule as MatInputModule } from '@angular/material/legacy-input';
import { MatLegacyButtonModule as MatButtonModule } from '@angular/material/legacy-button';
import { MatLegacyTableModule as MatTableModule } from '@angular/material/legacy-table';
import { MatRippleModule } from '@angular/material/core';
import { MatSortModule } from '@angular/material/sort';

@NgModule({
  declarations: [
    ContributorsComponent
  ],
  imports: [
    CommonModule,
    ContributorsRoutingModule,
    MatProgressBarModule,
    TranslateModule,
    MatPaginatorModule,
    MatIconModule,
    MatInputModule,
    MatButtonModule,
    MatTableModule,
    MatRippleModule,
    MatSortModule
  ]
})
export class ContributorsModule {
}
