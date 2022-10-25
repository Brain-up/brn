import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ContributorsComponent } from './contributors.component';

const routes: Routes = [
  {
    path: '',
    component: ContributorsComponent,
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ContributorsRoutingModule { }
