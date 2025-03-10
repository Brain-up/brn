import { Injectable, inject } from '@angular/core';
import { MatIconRegistry } from '@angular/material/icon';
import { DomSanitizer } from '@angular/platform-browser';

@Injectable({ providedIn: 'root' })
export class SvgIconsRegistrarService {
  private readonly matIconRegistry = inject(MatIconRegistry);
  private readonly domSanitizer = inject(DomSanitizer);

  private static readonly ICONS_FOLDER_PATH = 'assets/icons/';

  public registerIcons(): void {
    this.matIconRegistry
      .addSvgIcon(
        'arrow-back',
        this.domSanitizer.bypassSecurityTrustResourceUrl(
          SvgIconsRegistrarService.ICONS_FOLDER_PATH + 'arrow-back.svg',
        ),
      )
      .addSvgIcon(
        'expand-more',
        this.domSanitizer.bypassSecurityTrustResourceUrl(
          SvgIconsRegistrarService.ICONS_FOLDER_PATH + 'expand-more.svg',
        ),
      )
      .addSvgIcon(
        'file-download',
        this.domSanitizer.bypassSecurityTrustResourceUrl(
          SvgIconsRegistrarService.ICONS_FOLDER_PATH + 'file-download.svg',
        ),
      )
      .addSvgIcon(
        'foto-download',
        this.domSanitizer.bypassSecurityTrustResourceUrl(
          SvgIconsRegistrarService.ICONS_FOLDER_PATH + 'foto-download.svg',
        ),
      )
      .addSvgIcon(
        'help',
        this.domSanitizer.bypassSecurityTrustResourceUrl(
          SvgIconsRegistrarService.ICONS_FOLDER_PATH + 'help.svg',
        ),
      )
      .addSvgIcon(
        'left-arrow',
        this.domSanitizer.bypassSecurityTrustResourceUrl(
          SvgIconsRegistrarService.ICONS_FOLDER_PATH + 'left-arrow.svg',
        ),
      )
      .addSvgIcon(
        'logout',
        this.domSanitizer.bypassSecurityTrustResourceUrl(
          SvgIconsRegistrarService.ICONS_FOLDER_PATH + 'logout.svg',
        ),
      )
      .addSvgIcon(
        'right-arrow',
        this.domSanitizer.bypassSecurityTrustResourceUrl(
          SvgIconsRegistrarService.ICONS_FOLDER_PATH + 'right-arrow.svg',
        ),
      )
      .addSvgIcon(
        'star',
        this.domSanitizer.bypassSecurityTrustResourceUrl(
          SvgIconsRegistrarService.ICONS_FOLDER_PATH + 'star.svg',
        ),
      )
      .addSvgIcon(
        'up-arrow',
        this.domSanitizer.bypassSecurityTrustResourceUrl(
          SvgIconsRegistrarService.ICONS_FOLDER_PATH + 'up-arrow.svg',
        ),
      );
  }
}
