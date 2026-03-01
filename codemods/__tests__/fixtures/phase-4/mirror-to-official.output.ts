import Model from '@warp-drive/legacy/model';
import { attr } from '@warp-drive/legacy/model';
import { Type } from '@warp-drive/core/types/symbols';
import { withDefaults } from '@warp-drive/core/schema/construction';
import JSONAPIAdapter from '@warp-drive/legacy/adapter/json-api';

export default class Foo extends Model {
  declare [Type]: 'foo';
  @attr('string') name!: string;
}
