import { AuthenticationApiService } from '@auth/services/api/authentication-api.service';
import { ChangeDetectionStrategy, Component, OnInit, inject } from '@angular/core';
import { marker } from '@biesbjerg/ngx-translate-extract-marker';
import { TokenService } from '@root/services/token.service';
import { UserCredential } from '@root/models/auth-token';

@Component({
    selector: 'app-admin',
    templateUrl: './admin.component.html',
    styleUrls: ['./admin.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class AdminComponent implements OnInit {
  private readonly authenticationApiService = inject(AuthenticationApiService);
  private readonly tokenService = inject(TokenService);

  public adminName: UserCredential;
  public readonly mainTabs = [
    { label: marker('Admin.Menu.Users'), link: 'users' },
    { label: marker('Admin.Menu.Contributors'), link: 'contributors' },
    { label: marker('Admin.Menu.Exercises'), link: 'exercises' },
    { label: marker('Admin.Menu.Resources'), link: 'resources' },
    { label: marker('Admin.Menu.UploadFile'), link: 'upload-file' },
    { label: marker('Admin.Menu.Swagger'), link: 'swagger' },
  ];

  public ngOnInit(): void {
    this.getAdminName();
  }

  public logout(): void {
    this.authenticationApiService.signOut();
  }

  private getAdminName(): void {
    this.adminName = this.tokenService.getToken<UserCredential>('AUTH_TOKEN');
  }
}
