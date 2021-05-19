import { Injectable } from '@angular/core';
import { CanActivate, UrlTree, Router, CanLoad, Route, UrlSegment, ActivatedRouteSnapshot } from '@angular/router';
import { NOT_FOUND_PAGE } from '@shared/constants/common-constants';

@Injectable()
export class UserIdParamGuard implements CanLoad, CanActivate {
  private readonly USER_ID_SEGMENT_NUMBER = 1;

  constructor(private readonly router: Router) {}

  public canLoad(route: Route, segments: UrlSegment[]): true | UrlTree {
    return Number.isInteger(Number(segments[this.USER_ID_SEGMENT_NUMBER].path))
      ? true
      : this.router.createUrlTree([NOT_FOUND_PAGE]);
  }

  public canActivate(activatedRouteSnapshot: ActivatedRouteSnapshot): true | UrlTree {
    return Number.isInteger(Number(activatedRouteSnapshot.params.userId))
      ? true
      : this.router.createUrlTree([NOT_FOUND_PAGE]);
  }
}
