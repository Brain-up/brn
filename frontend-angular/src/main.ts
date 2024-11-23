import { enableProdMode, importProvidersFrom } from '@angular/core';
import { platformBrowserDynamic } from '@angular/platform-browser-dynamic';

import { AppModule } from './app/app.module';
import { environment } from './environments/environment';
import { HTTP_INTERCEPTORS, provideHttpClient, withInterceptorsFromDi, HttpClient } from '@angular/common/http';
import { AuthInterceptor } from '@root/interceptors/auth.interceptor';
import { ExceptionsInterceptor } from '@root/interceptors/exceptions.interceptor';
import { StripUndefinedParamsInterceptor } from '@root/interceptors/strip-undefined-params.interceptor';
import { BrowserModule, bootstrapApplication } from '@angular/platform-browser';
import { provideAnimations } from '@angular/platform-browser/animations';
import { AppRoutingModule } from './app/app-routing.module';
import { TranslateModule, TranslateLoader } from '@ngx-translate/core';
import { TranslateHttpLoader } from '@ngx-translate/http-loader';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatButtonModule } from '@angular/material/button';
import { AngularFireModule } from '@angular/fire';
import { environment as environment_1 } from 'src/environments/environment';
import { AngularFireAuthModule } from '@angular/fire/auth';
import { AppComponent } from './app/app.component';

if (environment.production) {
  enableProdMode();
}

bootstrapApplication(AppComponent, {
    providers: [
        importProvidersFrom(BrowserModule, AppRoutingModule, TranslateModule.forRoot({
            loader: {
                provide: TranslateLoader,
                useFactory: (httpClient: HttpClient) => new TranslateHttpLoader(httpClient, 'assets/i18n/', `.json?cacheOff=${process.env.CACHE_OFF}`),
                deps: [HttpClient],
            },
        }), MatSnackBarModule, MatButtonModule, AngularFireModule.initializeApp(environment.firebaseConfig), AngularFireAuthModule),
        { provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi: true },
        { provide: HTTP_INTERCEPTORS, useClass: ExceptionsInterceptor, multi: true },
        { provide: HTTP_INTERCEPTORS, useClass: StripUndefinedParamsInterceptor, multi: true },
        provideHttpClient(withInterceptorsFromDi()),
        provideAnimations()
    ]
})
  .catch(err => console.error(err));
