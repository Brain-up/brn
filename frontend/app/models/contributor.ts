import Model, { attr } from '@ember-data/model';
import { inject as service } from '@ember/service';
import UserDataService from 'brn/services/user-data';
export default class Contributor extends Model {
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

// DO NOT DELETE: this is how TypeScript knows how to look up your models.
declare module 'ember-data/types/registries/model' {
  export default interface ModelRegistry {
    contributor: Contributor;
  }
}
