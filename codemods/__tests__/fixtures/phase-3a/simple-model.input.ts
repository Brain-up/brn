import Model, { attr, belongsTo, hasMany } from '@warp-drive/legacy/model';
import { Type } from '@warp-drive/core/types/symbols';
import { tracked } from '@glimmer/tracking';
import { service } from '@ember/service';

export default class Signal extends Model {
  declare [Type]: 'signal';

  @attr('string') name!: string;
  @attr('number') frequency!: number;
  @attr('boolean') isActive!: boolean;
  @belongsTo('task', { async: false, inverse: null }) task!: Task;
  @tracked isPlaying = false;
  @service('audio-player') audioPlayer!: AudioPlayerService;

  get displayName() {
    return `Signal: ${this.name}`;
  }
}
