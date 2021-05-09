import { Injectable } from '@angular/core';
import { MatIconRegistry } from '@angular/material/icon';
import { DomSanitizer } from '@angular/platform-browser';

@Injectable()
export class SvgIconsRegistrarService {
  private static readonly ICONS_FOLDER_PATH = 'assets/icons/';

  constructor(private readonly matIconRegistry: MatIconRegistry, private readonly domSanitizer: DomSanitizer) {}

  public registerIcons(): void {
    this.matIconRegistry
      .addSvgIcon(
        'help',
        this.domSanitizer.bypassSecurityTrustResourceUrl(SvgIconsRegistrarService.ICONS_FOLDER_PATH + 'help.svg')
      )
      .addSvgIcon(
        'left-arrow',
        this.domSanitizer.bypassSecurityTrustResourceUrl(SvgIconsRegistrarService.ICONS_FOLDER_PATH + 'left-arrow.svg')
      )
      .addSvgIcon(
        'right-arrow',
        this.domSanitizer.bypassSecurityTrustResourceUrl(SvgIconsRegistrarService.ICONS_FOLDER_PATH + 'right-arrow.svg')
      );
  }
}
