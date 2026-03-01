import Model from '@warp-drive-mirror/legacy/model';
import { attr } from '@warp-drive-mirror/legacy/model';
import { Type } from '@warp-drive-mirror/core/types/symbols';
import { withDefaults } from '@warp-drive-mirror/core/schema/construction';
import JSONAPIAdapter from '@warp-drive-mirror/legacy/adapter/json-api';

export default class Foo extends Model {
  declare [Type]: 'foo';
  @attr('string') name!: string;
}
