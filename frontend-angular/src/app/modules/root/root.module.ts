import { HttpClient, HTTP_INTERCEPTORS } from '@angular/common/http';
import { NgModule } from '@angular/core';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { TranslateLoader, TranslateModule } from '@ngx-translate/core';
import { TranslateHttpLoader } from '@ngx-translate/http-loader';
import { AuthAccessGuard } from './guards/auth-access.guard';
import { GuestAccessGuard } from './guards/guest-access.guard';
import { AuthInterceptor } from './interceptors/auth.interceptor';
import { ExceptionsInterceptor } from './interceptors/exceptions.interceptor';
import { AuthTokenService } from './services/auth-token.service';
import { SnackBarService } from './services/snack-bar.service';
import { SvgIconsRegistrarService } from './services/svg-icons-registrar.service';

@NgModule({
  imports: [
    TranslateModule.forRoot({
      loader: {
        provide: TranslateLoader,
        useFactory: (httpClient: HttpClient) =>
          new TranslateHttpLoader(httpClient, 'assets/i18n/', `.json?cacheOff=${process.env.CACHE_OFF}`),
        deps: [HttpClient],
      },
    }),
    MatSnackBarModule,
  ],
  providers: [
    GuestAccessGuard,
    AuthAccessGuard,
    { provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi: true },
    { provide: HTTP_INTERCEPTORS, useClass: ExceptionsInterceptor, multi: true },
    AuthTokenService,
    SvgIconsRegistrarService,
    SnackBarService,
  ],
})
export class RootModule {}
