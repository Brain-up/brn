
import { ChangeDetectionStrategy, Component, inject, OnInit } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatToolbarModule } from '@angular/material/toolbar';
import { RouterModule } from '@angular/router';
import { AuthenticationApiService } from '@auth/services/api/authentication-api.service';
import { marker } from '@biesbjerg/ngx-translate-extract-marker';
import { TranslateModule } from '@ngx-translate/core';
import { UserCredential } from '@root/models/auth-token';
import { TokenService } from '@root/services/token.service';
import { ShortNamePipe } from '@shared/pipes/short-name.pipe';
import { AdminApiService } from './services/api/admin-api.service';
import { CloudApiService } from './services/api/cloud-api.service';
import { GroupApiService } from './services/api/group-api.service';
import { SeriesApiService } from './services/api/series-api.service';
import { SubGroupApiService } from './services/api/sub-group-api.service';

@Component({
  selector: 'app-admin',
  templateUrl: './admin.component.html',
  styleUrls: ['./admin.component.scss'],
  imports: [
    RouterModule,
    MatButtonModule,
    MatIconModule,
    MatToolbarModule,
    TranslateModule,
    ShortNamePipe
],
  providers: [
    AdminApiService,
    AuthenticationApiService,
    CloudApiService,
    GroupApiService,
    SeriesApiService,
    SubGroupApiService,
  ],
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
