import { Routes } from "@angular/router";
import { ContributorsComponent } from "./contributors.component";

export const CONTRIBUTORS_ROUTES: Routes = [
  {
    path: "",
    component: ContributorsComponent,
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
