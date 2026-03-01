import Controller from '@ember/controller';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { inject as service } from '@ember/service';
import Router from '@ember/routing/router-service';
import type { GroupRouteModel } from 'brn/routes/group';
import type { Group as GroupModel } from 'brn/schemas/group';
import type { Series as SeriesModel } from 'brn/schemas/series';

export default class GroupController extends Controller {
  @service router!: Router;

  declare model: GroupRouteModel | GroupModel;

  get group(): GroupModel {
    const m = this.model;
    if ('group' in m && 'series' in m) {
      return (m as GroupRouteModel).group;
    }
    return m as GroupModel;
  }

  get series(): SeriesModel[] {
    const m = this.model;
    if ('group' in m && 'series' in m) {
      return (m as GroupRouteModel).series;
    }
    return [];
  }

  get headerAndNavShown() {
    return (
      this.router.currentURL.includes('task') ||
      this.router.currentURL.includes('loading')
    );
  }
}
