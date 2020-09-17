import Controller from '@ember/controller';
import { tracked } from '@glimmer/tracking';
import { cached } from 'tracked-toolbox';

export default class GroupSeriesController extends Controller {
  queryParams = ['name']
  @tracked name = ''
  get exerciseName() {
    return this.name;
  }
  @cached
  get exerciseGroups() {
    const items = {};
    const exercises = this.model.exercises.toArray();
    exercises.forEach((el)=>{
      if (!(el.name in items)) {
        items[el.name] = {
          count: 0,
          name: el.name,
          picture: `/${el.pictureUrl}`
        }
      }
      items[el.name].count++;
    });
    return Object.values(items);
  }
}
