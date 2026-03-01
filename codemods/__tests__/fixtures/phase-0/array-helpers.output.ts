import Route from '@ember/routing/route';

export default class ExercisesRoute extends Route {
  async model() {
    const data = await this.store.findAll('exercise');
    const exercises = Array.from(data);
    const sorted = Array.from(exercises).sort((a, b) => a.order < b.order ? -1 : a.order > b.order ? 1 : 0);
    const names = exercises.map(item => item.name);
    const active = exercises.filter(item => // NOTE: Ember filterBy used == (loose equality)
    item.isActive === true);
    const truthy = exercises.filter(item => item.name);
    const unique = [...new Set(names)];
    return { exercises, sorted, names, active, truthy, unique };
  }
}
