import { Injectable } from '@angular/core';
import { UrlTree, Router } from '@angular/router';
import { UserCredential } from '@root/models/auth-token';
import { TokenService } from '@root/services/token.service';
import { HOME_PAGE_URL } from '@shared/constants/common-constants';

@Injectable()
export class GuestAccessGuard  {
  constructor(
    private readonly router: Router,
    private readonly tokenService: TokenService,
  ) {}

  public canLoad(): UrlTree | true {
    return this.tokenService.getToken<UserCredential>()
      ? this.router.createUrlTree([HOME_PAGE_URL])
      : true;
  }

  public canActivate(): UrlTree | true {
    return this.canLoad();
  }
}
