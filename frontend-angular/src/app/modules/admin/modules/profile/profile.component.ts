import { ChangeDetectionStrategy, Component, OnInit } from '@angular/core';
import { TokenService } from '@root/services/token.service';
import { UserCredential } from '@root/models/auth-token';

@Component({
  selector: 'app-profile',
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ProfileComponent implements OnInit {
  public adminData: UserCredential;
  constructor(private readonly tokenService: TokenService) {}

  ngOnInit(): void {
    this.getAdmindata();
  }

  private getAdmindata(): void {
    this.adminData = this.tokenService.getToken<UserCredential>('AUTH_TOKEN');
  }
}
