import { ChangeDetectionStrategy, Component } from '@angular/core';
import { AuthenticationApiService } from '@auth/services/api/authentication-api.service';
import { marker } from '@biesbjerg/ngx-translate-extract-marker';

@Component({
  selector: 'app-admin',
  templateUrl: './admin.component.html',
  styleUrls: ['./admin.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminComponent {
  constructor(
    private readonly authenticationApiService: AuthenticationApiService,
  ) {}

  public readonly mainTabs = [
    { label: marker('Admin.Menu.Users'), link: 'users' },
    { label: marker('Admin.Menu.Exercises'), link: 'exercises' },
    { label: marker('Admin.Menu.Resources'), link: 'resources' },
    { label: marker('Admin.Menu.UploadFile'), link: 'upload-file' },
  ];

  public logout(): void {
    this.authenticationApiService.signOut();
  }
}
