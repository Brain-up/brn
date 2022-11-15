import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ContributorsComponent } from './contributors.component';

const routes: Routes = [
  {
    path: '',
    component: ContributorsComponent,
  },
  {
    path: 'contributor/:contributorId', loadChildren: () => import('./module/contributor/contributor.module')
      .then(m => m.ContributorModule)
  },
  {
    path: 'contributor', loadChildren: () => import('./module/contributor/contributor.module')
      .then(m => m.ContributorModule)
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ContributorsRoutingModule {
}
