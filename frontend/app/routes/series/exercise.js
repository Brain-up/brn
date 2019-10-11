import Route from '@ember/routing/route';
import { inject } from '@ember/service';

export default Route.extend({
  store: inject(),

  model({exercise_id:exerciseId}) {
    return this.store.queryRecord('exercise',exerciseId);
  },

  async afterModel(exercise,{to}) {
    await this.store.query('task',{exerciseId:exercise.id});
    if (to.name.endsWith('exercise.index') && exercise.tasks.firstObject) {
      this.transitionTo('series.exercise.task', exercise.tasks.firstObject)
    }
  }
});
