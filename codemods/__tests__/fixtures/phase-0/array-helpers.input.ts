import Route from '@ember/routing/route';

export default class ExercisesRoute extends Route {
  async model() {
    const data = await this.store.findAll('exercise');
    const exercises = data.toArray();
    const sorted = exercises.sortBy('order');
    const names = exercises.mapBy('name');
    const active = exercises.filterBy('isActive', true);
    const truthy = exercises.filterBy('name');
    const unique = names.uniq();
    return { exercises, sorted, names, active, truthy, unique };
  }
}
