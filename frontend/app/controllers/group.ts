import Controller from '@ember/controller';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { inject as service } from '@ember/service';
import Router from '@ember/routing/router-service';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { tracked } from '@glimmer/tracking';
import type SeriesModel from 'brn/models/series';

export default class GroupController extends Controller {
  @service router!: Router;

  // Loaded series from the route's afterModel, used as a reliable fallback
  // in case group.series (hasMany) isn't resolved from the cache.
  @tracked loadedSeries: SeriesModel[] = [];

  get headerAndNavShown() {
    return (
      this.router.currentURL.includes('task') ||
      this.router.currentURL.includes('loading')
    );
  }
}
