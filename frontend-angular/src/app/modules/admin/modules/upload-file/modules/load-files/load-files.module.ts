import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';

import { LoadFilesRoutingModule } from './load-files-routing.module';
import { LoadFilesComponent } from './load-files.component';

@NgModule({
    imports: [CommonModule, ReactiveFormsModule, LoadFilesRoutingModule, TranslateModule, LoadFilesComponent],
})
export class LoadFilesModule {}
