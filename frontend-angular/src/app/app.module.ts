import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { NgModule, inject } from '@angular/core';
import { HttpClient, HTTP_INTERCEPTORS, provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
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
import { AngularFireModule } from '@angular/fire';
import { AngularFireAuthModule } from '@angular/fire/auth';
import { environment } from 'src/environments/environment';

@NgModule(/* TODO(standalone-migration): clean up removed NgModule class manually. 
{ declarations: [AppComponent],
    bootstrap: [AppComponent], imports: [BrowserModule,
        BrowserAnimationsModule,
        AppRoutingModule,
        TranslateModule.forRoot({
            loader: {
                provide: TranslateLoader,
                useFactory: (httpClient: HttpClient) => new TranslateHttpLoader(httpClient, 'assets/i18n/', `.json?cacheOff=${process.env.CACHE_OFF}`),
                deps: [HttpClient],
            },
        }),
        MatSnackBarModule,
        MatButtonModule,
        AngularFireModule.initializeApp(environment.firebaseConfig),
        AngularFireAuthModule, NotFoundComponent], providers: [
        { provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi: true },
        { provide: HTTP_INTERCEPTORS, useClass: ExceptionsInterceptor, multi: true },
        { provide: HTTP_INTERCEPTORS, useClass: StripUndefinedParamsInterceptor, multi: true },
        provideHttpClient(withInterceptorsFromDi()),
    ] } */)
export class AppModule {
  constructor() {
    const translateService = inject(TranslateService);
    const svgIconsRegistrarService = inject(SvgIconsRegistrarService);

    translateService.setDefaultLang(ALocaleStorage.LANG.get() ?? DEFAULT_LANG);
    dayjs.locale(translateService.defaultLang);

    svgIconsRegistrarService.registerIcons();
  }
}
