import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LoadFilesComponent } from './load-files.component';

const routes: Routes = [
  {
    path: '',
    component: LoadFilesComponent,
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class LoadFilesRoutingModule {}
