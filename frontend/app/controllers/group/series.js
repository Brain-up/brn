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
        const detail = el.name.indexOf('/') > 0 ? el.name.slice(el.name.indexOf('/'), el.name.length): '-';
        items[el.name] = {
          count: 0,
          name: el.name.replace(detail, '').trim(),
          fullName: el.name,
          detail: detail.trim(),
          picture: `/${el.pictureUrl}`
        }
      }
      items[el.name].count++;
    });
    return Object.values(items);
  }
}
