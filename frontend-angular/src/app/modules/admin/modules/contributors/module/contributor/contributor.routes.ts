import { Routes } from "@angular/router";


export const CONTRIBUTOR_ROUTES: Routes = [
  { path: "", loadComponent: () => import('./contributor.component').then(m => m.ContributorComponent) },
];
