import Model, {
  // belongsTo,
  attr,
} from '@warp-drive-mirror/legacy/model';
import { Type } from '@warp-drive-mirror/core/types/symbols';

export default class HeadphoneModel extends Model {
  declare [Type]: 'headphone';
  @attr('string')
  description!: string;
  @attr('string')
  name!: string;
  // @belongsTo('user', { async: false })
  // user!: User;
}

