import { Routes } from "@angular/router";

export const RESOURCES_ROUTES: Routes = [
  {
    path: "",
    loadComponent: () => import('./resources.component').then(m => m.ResourcesComponent),
  },
  {
    path: "resource/:resourcesId",
    loadComponent: () =>
      import("./module/resources/resources.component").then(
        (m) => m.ResourcesComponent
      ),
    loadChildren: () =>
      import("./module/resources/resources.routes").then(
        (m) => m.RESOURCES_ROUTES
      ),
  },
  {
    path: "resource",
    loadComponent: () =>
      import("./module/resources/resources.component").then(
        (m) => m.ResourcesComponent
      ),
    loadChildren: () =>
      import("./module/resources/resources.routes").then(
        (m) => m.RESOURCES_ROUTES
      ),
  },
];
