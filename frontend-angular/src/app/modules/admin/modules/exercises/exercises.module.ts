import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatSortModule } from '@angular/material/sort';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';

import { ExercisesComponent } from './components/exercises/exercises.component';
import { SelectPanelComponent } from './components/exercises/select-panel/select-panel.component';
import { ExercisesRoutingModule } from './exercises-routing.module';

@NgModule({
  declarations: [ExercisesComponent, SelectPanelComponent],
  imports: [
    CommonModule,
    ExercisesRoutingModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatSelectModule,
    MatIconModule,
    MatTableModule,
    MatSortModule,
    MatSlideToggleModule
  ]
})
export class ExercisesModule { }
