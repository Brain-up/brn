import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { NgModule } from '@angular/core';
import { HttpClient, HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';
import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { SvgIconsRegistrarService } from '@root/services/svg-icons-registrar.service';
import { TranslateLoader, TranslateModule, TranslateService } from '@ngx-translate/core';
import { ALocaleStorage } from '@shared/storages/local-storage';
import { DEFAULT_LANG } from '@shared/constants/common-constants';
import 'dayjs/locale/ru';
import * as dayjs from 'dayjs';
import { NotFoundComponent } from '@root/components/not-found/not-found.component';
import { TranslateHttpLoader } from '@ngx-translate/http-loader';
import { MatButtonModule } from '@angular/material/button';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { AuthInterceptor } from '@root/interceptors/auth.interceptor';
import { ExceptionsInterceptor } from '@root/interceptors/exceptions.interceptor';
import { StripUndefinedParamsInterceptor } from '@root/interceptors/strip-undefined-params.interceptor';

@NgModule({
  declarations: [AppComponent, NotFoundComponent],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    HttpClientModule,
    AppRoutingModule,
    TranslateModule.forRoot({
      loader: {
        provide: TranslateLoader,
        useFactory: (httpClient: HttpClient) =>
          new TranslateHttpLoader(httpClient, 'assets/i18n/', `.json?cacheOff=${process.env.CACHE_OFF}`),
        deps: [HttpClient],
      },
    }),
    MatSnackBarModule,
    MatButtonModule,
  ],
  providers: [
    { provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi: true },
    { provide: HTTP_INTERCEPTORS, useClass: ExceptionsInterceptor, multi: true },
    { provide: HTTP_INTERCEPTORS, useClass: StripUndefinedParamsInterceptor, multi: true },
  ],
  bootstrap: [AppComponent],
})
export class AppModule {
  constructor(translateService: TranslateService, svgIconsRegistrarService: SvgIconsRegistrarService) {
    translateService.setDefaultLang(ALocaleStorage.LANG.get() ?? DEFAULT_LANG);
    dayjs.locale(translateService.defaultLang);

    svgIconsRegistrarService.registerIcons();
  }
}
