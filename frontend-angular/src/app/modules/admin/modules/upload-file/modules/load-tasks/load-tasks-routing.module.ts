import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LoadTasksComponent } from './load-tasks.component';

const routes: Routes = [
  {
    path: '',
    component: LoadTasksComponent,
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class LoadTasksRoutingModule {}
