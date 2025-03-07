import { Routes } from "@angular/router";


export const CONTRIBUTORS_ROUTES: Routes = [
  {
    path: "",
    loadComponent: () => import('./contributors.component').then(m => m.ContributorsComponent),
  },
  {
    path: "contributor/:contributorId",
    loadComponent: () =>
      import("./module/contributor/contributor.component").then(
        (m) => m.ContributorComponent
      ),
    loadChildren: () =>
      import("./module/contributor/contributor.routes").then(
        (m) => m.CONTRIBUTOR_ROUTES
      ),
  },
  {
    path: "contributor",
    loadComponent: () =>
      import("./module/contributor/contributor.component").then(
        (m) => m.ContributorComponent
      ),
    loadChildren: () =>
      import("./module/contributor/contributor.routes").then(
        (m) => m.CONTRIBUTOR_ROUTES
      ),
  },
];
