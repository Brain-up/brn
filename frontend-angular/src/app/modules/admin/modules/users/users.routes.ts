import { Routes } from "@angular/router";


export const USERS_ROUTES: Routes = [
  {
    path: "",
    loadComponent: () => import('./users.component').then(m => m.UsersComponent),
  },
  {
    path: ":userId/statistics",
    loadChildren: () =>
      import("./modules/statistics/statistics.routes").then(
        (m) => m.STATISTICS_ROUTES
      ),
  },
];
