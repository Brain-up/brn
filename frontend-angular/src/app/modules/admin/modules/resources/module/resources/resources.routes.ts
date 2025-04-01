import { Routes } from "@angular/router";


export const RESOURCES_ROUTES: Routes = [
  { path: "", loadComponent: () => import('./resources.component').then(m => m.ResourcesComponent) },
];
