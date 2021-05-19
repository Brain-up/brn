import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ExercisesComponent } from './components/exercises/exercises.component';

const routes: Routes = [
  {
    path: '',
    component: ExercisesComponent,
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class ExercisesRoutingModule {}
