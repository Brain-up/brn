import { Injectable } from '@angular/core';
import { CanActivate, UrlTree, Router, CanLoad } from '@angular/router';
import { AuthTokenService } from '@root/services/auth-token.service';
import { AUTH_PAGE } from '@shared/constants/common-constants';

@Injectable()
export class AuthAccessGuard implements CanLoad, CanActivate {
  constructor(private readonly router: Router, private readonly authTokenService: AuthTokenService) {}

  public canLoad(): true | UrlTree {
    return this.authTokenService.getAuthToken() ? true : this.router.createUrlTree([AUTH_PAGE]);
  }

  public canActivate(): true | UrlTree {
    return this.canLoad();
  }
}
