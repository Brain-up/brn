import { AuthenticationApiService } from '@auth/services/api/authentication-api.service';
import { ChangeDetectionStrategy, Component, OnInit } from '@angular/core';
import { marker } from '@biesbjerg/ngx-translate-extract-marker';
import { TokenService } from '@root/services/token.service';
import { UserCredential } from '@root/models/auth-token';

@Component({
  selector: 'app-admin',
  templateUrl: './admin.component.html',
  styleUrls: ['./admin.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminComponent implements OnInit {
  public adminName: UserCredential;

  constructor(
    private readonly authenticationApiService: AuthenticationApiService,
    private readonly tokenService: TokenService,
  ) {}

  public ngOnInit(): void {
    this.getAdminName();
  }

  public readonly mainTabs = [
    { label: marker('Admin.Menu.Users'), link: 'users' },
    { label: marker('Admin.Menu.Exercises'), link: 'exercises' },
    { label: marker('Admin.Menu.Resources'), link: 'resources' },
    { label: marker('Admin.Menu.UploadFile'), link: 'upload-file' },
  ];

  public logout(): void {
    this.authenticationApiService.signOut();
  }

  private getAdminName(): void {
    this.adminName = this.tokenService.getToken<UserCredential>('AUTH_TOKEN');
  }
}
