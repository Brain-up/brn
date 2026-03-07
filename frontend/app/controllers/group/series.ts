import Controller from '@ember/controller';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { service } from '@ember/service';
import type Router from '@ember/routing/router-service';
import { cached } from 'tracked-toolbox';
import type { Subgroup } from 'brn/schemas/subgroup';

export default class GroupSeriesController extends Controller {
  @service router!: Router;
  declare model: Iterable<Subgroup>;

  get isIndexActive(): boolean {
    return this.router.currentRouteName === 'group.series.index';
  }

  @cached
  get exerciseSubGroups(): Subgroup[] {
    const exercises = Array.from(this.model) as Subgroup[];
    return exercises;
  }
}
