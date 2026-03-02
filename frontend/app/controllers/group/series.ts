import Controller from '@ember/controller';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { cached } from 'tracked-toolbox';
import type { Subgroup } from 'brn/schemas/subgroup';

export default class GroupSeriesController extends Controller {
  declare model: Iterable<Subgroup>;

  @cached
  get exerciseSubGroups(): Subgroup[] {
    const exercises = Array.from(this.model) as Subgroup[];
    return exercises;
  }
}
