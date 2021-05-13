import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LoadFileComponent } from './load-file.component';

const routes: Routes = [
  {
    path: '',
    component: LoadFileComponent,
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class LoadFileRoutingModule {}
