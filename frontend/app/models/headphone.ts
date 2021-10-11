import Model, {
  // belongsTo,
  attr,
} from '@ember-data/model';

export default class HeadphoneModel extends Model {
  @attr('string')
  description!: string;
  @attr('string')
  name!: string;
  // @belongsTo('user', { async: false })
  // user!: User;
}

declare module 'ember-data/types/registries/model' {
  export default interface ModelRegistry {
    headphone: HeadphoneModel;
  }
}
