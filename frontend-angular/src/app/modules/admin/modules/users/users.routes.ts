import { Routes } from "@angular/router";
import { UsersComponent } from "./users.component";

export const USERS_ROUTES: Routes = [
  {
    path: "",
    component: UsersComponent,
  },
  {
    path: ":userId/statistics",
    loadChildren: () =>
      import("./modules/statistics/statistics.routes").then(
        (m) => m.STATISTICS_ROUTES
      ),
  },
];
