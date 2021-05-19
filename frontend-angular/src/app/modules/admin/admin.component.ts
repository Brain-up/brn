import { ChangeDetectionStrategy, Component } from '@angular/core';
import { marker } from '@biesbjerg/ngx-translate-extract-marker';

@Component({
  selector: 'app-admin',
  templateUrl: './admin.component.html',
  styleUrls: ['./admin.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminComponent {
  public readonly mainTabs = [
    { label: marker('Admin.Menu.Users'), link: 'users' },
    { label: marker('Admin.Menu.Exercises'), link: 'exercises' },
    { label: marker('Admin.Menu.Resources'), link: 'resources' },
    { label: marker('Admin.Menu.UploadFile'), link: 'upload-file' },
  ];
}
