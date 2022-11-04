import Model, {
    attr,
  } from '@ember-data/model';
  import { inject as service } from '@ember/service';
  import Intl from 'ember-intl/services/intl';



export default class Contributor extends Model {
    @service('intl') intl!: Intl;
    @attr() rawName!: Record<string, string>;
    @attr() rawDescription!: Record<string, string>;
    @attr() rawCompany!: Record<string, string>;
    @attr('string') avatar!: string;
    @attr('number') contribution!: number;
    @attr('boolean') isActive!: boolean;
    @attr('string') kind!: "DEVELOPER" | "SPECIALIST";    
    @attr('array') contacts!: string[];

    get locale() {
        return this.intl.locale[0];
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
  