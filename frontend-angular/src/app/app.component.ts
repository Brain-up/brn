import { Component, inject } from "@angular/core";
import { RouterOutlet } from "@angular/router";
import { TranslateService } from "@ngx-translate/core";
import { SvgIconsRegistrarService } from "@root/services/svg-icons-registrar.service";
import { DEFAULT_LANG } from "@shared/constants/common-constants";
import { ALocaleStorage } from "@shared/storages/local-storage";
import dayjs from "dayjs";

@Component({
  selector: "app-root",
  templateUrl: "./app.component.html",
  styleUrls: ["./app.component.scss"],
  imports: [RouterOutlet],
})
export class AppComponent {
  constructor() {
    const translateService = inject(TranslateService);
    const svgIconsRegistrarService = inject(SvgIconsRegistrarService);

    translateService.setDefaultLang(ALocaleStorage.LANG.get() ?? DEFAULT_LANG);
    dayjs.locale(translateService.defaultLang);

    svgIconsRegistrarService.registerIcons();
  }
}
