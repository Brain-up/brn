import Route from '@ember/routing/route';
import { inject } from '@ember/service';

export default Route.extend({
  store: inject(),

  async afterModel(series, {to}) {
    await this.store.query('exercise',{});
    if (to.name === 'series.index' && series.exercises.firstObject) {
      this.transitionTo('series.exercise', series.exercises.firstObject)
    }
  },

});
