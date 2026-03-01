import Route from '@ember/routing/route';
import type Store from 'brn/services/store';
import type Exercise from 'brn/models/exercise';

export default class ExercisesRoute extends Route {
  store!: Store;

  async model(): Promise<Exercise[]> {
    const data = await this.store.findAll('exercise');
    return Array.from(data);
  }
}
