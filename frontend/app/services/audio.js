import Service from '@ember/service';
import { tracked } from '@glimmer/tracking';
export default class AudioService extends Service {
  @tracked
  player = null;
  register(player) {
    this.player = player;
  }
  get isPlaying() {
    return this.player.isPlaying;
  }
}
