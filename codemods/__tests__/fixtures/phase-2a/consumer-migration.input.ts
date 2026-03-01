import Route from '@ember/routing/route';
import Store from '@ember-data/store';
import Exercise from 'brn/models/exercise';

export default class ExercisesRoute extends Route {
  store!: Store;

  async model(): Promise<Exercise[]> {
    const data = await this.store.findAll('exercise');
    return data.toArray();
  }
}
