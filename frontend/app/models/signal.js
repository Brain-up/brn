import Model, { attr } from '@ember-data/model';

export default class SignalModel extends Model {
  @attr('number') frequency;
  @attr('number') duration;
}
