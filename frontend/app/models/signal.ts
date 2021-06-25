import Model, { attr } from '@ember-data/model';

export default class Signal extends Model {
  @attr('number') frequency!: number;
  @attr('number') duration!: number;
}

// DO NOT DELETE: this is how TypeScript knows how to look up your models.
declare module 'ember-data/types/registries/model' {
  export default interface ModelRegistry {
    signal: Signal;
  }
}
