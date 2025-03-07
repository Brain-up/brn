import { ChangeDetectionStrategy, Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { HOME_PAGE_URL } from '@shared/constants/common-constants';

@Component({
  selector: 'app-not-found',
  templateUrl: './not-found.component.html',
  styleUrls: ['./not-found.component.scss'],
  imports: [TranslateModule, RouterLink],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class NotFoundComponent {
  public readonly HOME_PAGE_URL = HOME_PAGE_URL;
  public readonly digits = new Array(50).fill({ four: 4, zero: 0 });
}
