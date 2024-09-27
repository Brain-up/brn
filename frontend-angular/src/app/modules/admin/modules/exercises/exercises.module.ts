import { CommonModule } from '@angular/common';
import { ExercisesComponent } from './components/exercises/exercises.component';
import { ExercisesRoutingModule } from './exercises-routing.module';
import { MatLegacyFormFieldModule as MatFormFieldModule } from '@angular/material/legacy-form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatLegacySelectModule as MatSelectModule } from '@angular/material/legacy-select';
import { MatLegacySlideToggleModule as MatSlideToggleModule } from '@angular/material/legacy-slide-toggle';
import { MatSortModule } from '@angular/material/sort';
import { MatLegacyTableModule as MatTableModule } from '@angular/material/legacy-table';
import { NgModule } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { SelectPanelComponent } from './components/exercises/select-panel/select-panel.component';
import { TranslateModule } from '@ngx-translate/core';

@NgModule({
  declarations: [ExercisesComponent, SelectPanelComponent],
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
    TranslateModule
  ],
})
export class ExercisesModule {}
