import Model, { attr } from '@warp-drive-mirror/legacy/model';
import { Type } from '@warp-drive-mirror/core/types/symbols';
import { inject as service } from '@ember/service';
import UserDataService from 'brn/services/user-data';
export default class Contributor extends Model {
  declare [Type]: 'contributor';
  @service('user-data') userData!: UserDataService;
  @attr() rawName!: Record<string, string>;
  @attr() rawDescription!: Record<string, string>;
  @attr() rawCompany!: Record<string, string>;
  @attr('string') avatar!: string;
  @attr('number') contribution!: number;
  @attr('boolean') isActive!: boolean;
  @attr('string') login!: string;
  @attr('string') kind!:
    | 'DEVELOPER'
    | 'SPECIALIST'
    | 'QA'
    | 'DESIGNER'
    | 'OTHER';
  @attr('array') contacts!: {type: string, value: string}[];

  get locale() {
    return this.userData.activeLocale;
  }
  get name() {
    return this.rawName[this.locale] ?? '';
  }
  get description() {
    return this.rawDescription[this.locale] ?? '';
  }
  get company() {
    return this.rawCompany[this.locale] ?? '';
  }
}

