import Controller from '@ember/controller';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { service } from '@ember/service';
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
    const name = this.router.currentRouteName ?? '';
    return name.includes('task') || name === 'group.loading';
  }

  get isInSubgroupView() {
    const name = this.router.currentRouteName ?? '';
    return name.includes('subgroup') && !name.includes('exercise');
  }
}
