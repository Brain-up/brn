import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { MatSelectModule } from '@angular/material/select';
import { TranslateModule } from '@ngx-translate/core';

import { LoadTasksRoutingModule } from './load-tasks-routing.module';
import { LoadTasksComponent } from './load-tasks.component';

@NgModule({
    imports: [
    CommonModule,
    ReactiveFormsModule,
    LoadTasksRoutingModule,
    TranslateModule,
    MatSelectModule,
    LoadTasksComponent,
],
})
export class LoadTasksModule {}
