import Model, { attr } from '@warp-drive-mirror/legacy/model';
import { Type } from '@warp-drive-mirror/core/types/symbols';

export default class Signal extends Model {
  declare [Type]: 'signal';
  @attr('number') frequency!: number;
  @attr('number') duration!: number;
}

