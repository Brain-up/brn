import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { NgModule } from '@angular/core';
import { HttpClientModule } from '@angular/common/http';
import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { RootModule } from '@root/root.module';
import { SvgIconsRegistrarService } from '@root/services/svg-icons-registrar.service';
import { TranslateService } from '@ngx-translate/core';
import { ALocaleStorage } from '@shared/storages/local-storage';
import { DEFAULT_LANG } from '@shared/constants/common-constants';
import 'dayjs/locale/ru';
import * as dayjs from 'dayjs';

@NgModule({
  declarations: [AppComponent],
  imports: [BrowserModule, BrowserAnimationsModule, HttpClientModule, RootModule, AppRoutingModule],
  bootstrap: [AppComponent],
})
export class AppModule {
  constructor(translateService: TranslateService, svgIconsRegistrarService: SvgIconsRegistrarService) {
    translateService.setDefaultLang(ALocaleStorage.LANG.get() ?? DEFAULT_LANG);
    dayjs.locale(translateService.defaultLang);
    svgIconsRegistrarService.registerIcons();
  }
}
