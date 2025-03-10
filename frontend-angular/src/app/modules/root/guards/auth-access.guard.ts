import { AUTH_PAGE_URL } from '@shared/constants/common-constants';
import { Router, UrlTree } from '@angular/router';
import { Injectable, inject } from '@angular/core';
import { TokenService } from '@root/services/token.service';
import { UserCredential } from '@root/models/auth-token';

@Injectable()
export class AuthAccessGuard  {
  private readonly tokenService = inject(TokenService);
  private readonly router = inject(Router);


  public canLoad(): true | UrlTree {
    return this.tokenService.getToken<UserCredential>()
      ? true
      : this.router.createUrlTree([AUTH_PAGE_URL]);
  }

  public canActivate(): true | UrlTree {
    return this.canLoad();
  }
}
