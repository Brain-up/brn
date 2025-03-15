import {
  HTTP_INTERCEPTORS,
  HttpClient,
  provideHttpClient,
  withInterceptorsFromDi,
} from "@angular/common/http";
import { enableProdMode, importProvidersFrom } from "@angular/core";
import { AngularFireModule } from "@angular/fire/compat";
import { AngularFireAuthModule } from "@angular/fire/compat/auth";
import { MatSnackBarModule } from "@angular/material/snack-bar";
import { BrowserModule, bootstrapApplication } from "@angular/platform-browser";
import { provideAnimations } from "@angular/platform-browser/animations";
import { provideRouter } from "@angular/router";
import { TranslateLoader, TranslateModule } from "@ngx-translate/core";
import { TranslateHttpLoader } from "@ngx-translate/http-loader";
import { AuthAccessGuard } from "@root/guards/auth-access.guard";
import { GuestAccessGuard } from "@root/guards/guest-access.guard";
import { AuthInterceptor } from "@root/interceptors/auth.interceptor";
import { ExceptionsInterceptor } from "@root/interceptors/exceptions.interceptor";
import { StripUndefinedParamsInterceptor } from "@root/interceptors/strip-undefined-params.interceptor";
import { AppComponent } from "./app/app.component";
import { APP_ROUTES } from "./app/app.routes";
import { environment } from "./environments/environment";

if (environment.production) {
  enableProdMode();
}

bootstrapApplication(AppComponent, {
  providers: [
    importProvidersFrom(
      BrowserModule,
      TranslateModule.forRoot({
        loader: {
          provide: TranslateLoader,
          useFactory: (httpClient: HttpClient) =>
            new TranslateHttpLoader(
              httpClient,
              "assets/i18n/",
              `.json?cacheOff=${process.env.CACHE_OFF}`
            ),
          deps: [HttpClient],
        },
      }),
      MatSnackBarModule,
      AngularFireModule.initializeApp(environment.firebaseConfig),
      AngularFireAuthModule
    ),
    { provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi: true },
    {
      provide: HTTP_INTERCEPTORS,
      useClass: ExceptionsInterceptor,
      multi: true,
    },
    {
      provide: HTTP_INTERCEPTORS,
      useClass: StripUndefinedParamsInterceptor,
      multi: true,
    },
    provideHttpClient(withInterceptorsFromDi()),
    provideAnimations(),
    provideRouter(APP_ROUTES),
    GuestAccessGuard,
    AuthAccessGuard,
  ],
}).catch((err) => console.error(err));
