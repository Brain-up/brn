import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ContributorComponent } from './contributor.component';

const routes: Routes = [{ path: '', component: ContributorComponent }];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ContributorRoutingModule { }
