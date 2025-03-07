import { Routes } from '@angular/router';


export const LOAD_FILES_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./load-files.component').then(m => m.LoadFilesComponent),
  },
];
