import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ContributorsRoutingModule } from './contributors-routing.module';
import { ContributorsComponent } from './contributors.component';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { TranslateModule } from '@ngx-translate/core';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatTableModule } from '@angular/material/table';
import { MatRippleModule } from '@angular/material/core';
import { MatSortModule } from '@angular/material/sort';

@NgModule({
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
        MatSortModule,
        ContributorsComponent
    ]
})
export class ContributorsModule {
}
