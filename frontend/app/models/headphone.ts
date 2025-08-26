import Model, {
  // belongsTo,
  attr,
} from '@ember-data/model';

export default class HeadphoneModel extends Model {
  @attr('string', { defaultValue: '' }) description!: string;
  @attr('string') name!: string;
  @attr('boolean', { defaultValue: true }) active!: boolean;
  @attr('string') type!: 'ON_EAR_BLUETOOTH';

  // @belongsTo('user', { async: false })
  // user!: User;
}

declare module 'ember-data/types/registries/model' {
  export default interface ModelRegistry {
    headphone: HeadphoneModel;
  }
}
