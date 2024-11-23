import { CommonModule } from '@angular/common';
import { ExercisesComponent } from './components/exercises/exercises.component';
import { ExercisesRoutingModule } from './exercises-routing.module';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatSelectModule } from '@angular/material/select';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatSortModule } from '@angular/material/sort';
import { MatTableModule } from '@angular/material/table';
import { NgModule } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { SelectPanelComponent } from './components/exercises/select-panel/select-panel.component';
import { TranslateModule } from '@ngx-translate/core';

@NgModule({
    imports: [
        CommonModule,
        ExercisesRoutingModule,
        MatFormFieldModule,
        MatIconModule,
        MatSelectModule,
        MatSlideToggleModule,
        MatSortModule,
        MatTableModule,
        ReactiveFormsModule,
        TranslateModule,
        ExercisesComponent, SelectPanelComponent
    ],
})
export class ExercisesModule {}
