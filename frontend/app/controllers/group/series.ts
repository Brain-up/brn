import Controller from '@ember/controller';
import { cached } from 'tracked-toolbox';

export default class GroupSeriesController extends Controller {
  @cached
  get exerciseSubGroups() {
    const exercises = Array.from(this.model);
    return exercises;
  }
}
