import { Injectable } from '@angular/core';
import { CanActivate, UrlTree, Router, CanLoad } from '@angular/router';
import { AuthTokenService } from '@root/services/auth-token.service';
import { HOME_PAGE_URL } from '@shared/constants/common-constants';

@Injectable()
export class GuestAccessGuard implements CanLoad, CanActivate {
  constructor(private readonly router: Router, private readonly authTokenService: AuthTokenService) {}

  public canLoad(): UrlTree | true {
    return this.authTokenService.getAuthToken() ? this.router.createUrlTree([HOME_PAGE_URL]) : true;
  }

  public canActivate(): UrlTree | true {
    return this.canLoad();
  }
}
