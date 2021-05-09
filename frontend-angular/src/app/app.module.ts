import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { NgModule } from '@angular/core';
import { HttpClientModule } from '@angular/common/http';
import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { RootModule } from '@root/root.module';
import { SvgIconsRegistrarService } from '@root/services/svg-icons-registrar.service';

@NgModule({
  declarations: [AppComponent],
  imports: [BrowserModule, BrowserAnimationsModule, HttpClientModule, RootModule, AppRoutingModule],
  bootstrap: [AppComponent],
})
export class AppModule {
  constructor(svgIconsRegistrarService: SvgIconsRegistrarService) {
    svgIconsRegistrarService.registerIcons();
  }
}
