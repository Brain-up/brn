import Route from '@ember/routing/route';
import { inject } from '@ember/service';

export default Route.extend({
  store: inject(),

  model({ series_id }) {
    return this.store.findRecord('series', series_id, { include: 'exercises' });
  },

  async afterModel(series, { to }) {
    // in case if series was pre-loaded
    // asking for exercises again
    await series.hasMany('exercises').load();
    if (to.name === 'series.index' && series.exercises.firstObject) {
      this.transitionTo('series.exercise', series.exercises.firstObject);
    }
  },
});
