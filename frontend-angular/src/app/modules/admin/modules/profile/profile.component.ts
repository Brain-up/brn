
import { ChangeDetectionStrategy, Component, OnInit, inject } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { TranslateModule } from '@ngx-translate/core';
import { UserCredential } from '@root/models/auth-token';
import { TokenService } from '@root/services/token.service';
import { ShortNamePipe } from '@shared/pipes/short-name.pipe';

@Component({
  selector: 'app-profile',
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.scss'],
  imports: [
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    TranslateModule,
    ShortNamePipe
],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ProfileComponent implements OnInit {
  private readonly tokenService = inject(TokenService);

  public adminData: UserCredential;

  ngOnInit(): void {
    this.getAdmindata();
  }

  private getAdmindata(): void {
    this.adminData = this.tokenService.getToken<UserCredential>('AUTH_TOKEN');
  }
}
