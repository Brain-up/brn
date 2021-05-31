import { Component, ChangeDetectionStrategy } from '@angular/core';
import { HOME_PAGE_URL } from '@shared/constants/common-constants';

@Component({
  selector: 'app-not-found',
  templateUrl: './not-found.component.html',
  styleUrls: ['./not-found.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class NotFoundComponent {
  public readonly HOME_PAGE_URL = HOME_PAGE_URL;
  public readonly digits = new Array(50).fill({ four: 4, zero: 0 });
}
