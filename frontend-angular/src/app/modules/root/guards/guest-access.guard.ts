import { Injectable } from '@angular/core';
import { CanActivate, UrlTree, Router, CanLoad } from '@angular/router';
import { UserCredential } from '@root/models/auth-token';
import { TokenService } from '@root/services/token.service';
import { HOME_PAGE_URL } from '@shared/constants/common-constants';

@Injectable()
export class GuestAccessGuard implements CanLoad, CanActivate {
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
