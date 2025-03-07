import { Routes } from "@angular/router";
import { AdminComponent } from "./admin.component";

export const ADMIN_ROUTES: Routes = [
  {
    path: "",
    component: AdminComponent,
    children: [
      {
        path: "",
        pathMatch: "full",
        redirectTo: "users",
      },
      {
        path: "users",
        // loadComponent: () =>
        //   import("./modules/users/users.component").then(
        //     (m) => m.UsersComponent
        //   ),
        loadChildren: () =>
          import("./modules/users/users.routes").then((m) => m.USERS_ROUTES),
      },
      {
        path: "contributors",
        loadComponent: () =>
          import("./modules/contributors/contributors.component").then(
            (m) => m.ContributorsComponent
          ),
        loadChildren: () =>
          import("./modules/contributors/contributors.routes").then(
            (m) => m.CONTRIBUTORS_ROUTES
          ),
      },
      {
        path: "exercises",
        loadComponent: () =>
          import("./modules/exercises/exercises.component").then(
            (m) => m.ExercisesComponent
          ),
      },
      {
        path: "profile",
        loadComponent: () =>
          import("./modules/profile/profile.component").then(
            (m) => m.ProfileComponent
          ),
        loadChildren: () =>
          import("./modules/profile/profile.routes").then(
            (m) => m.PROFILE_ROUTES
          ),
      },
      {
        path: "upload-file",
        loadComponent: () =>
          import("./modules/upload-file/upload-file.component").then(
            (m) => m.UploadFileComponent
          ),
        loadChildren: () =>
          import("./modules/upload-file/upload-file.routes").then(
            (m) => m.UPLOAD_FILE_ROUTES
          ),
      },
      {
        path: "swagger",
        loadComponent: () =>
          import("./modules/swagger/swagger.component").then(
            (m) => m.SwaggerComponent
          ),
      },
    ],
  },
];
