import Service from '@ember/service';
import { reads } from '@ember/object/computed';

export default Service.extend({
  init() {
    this._super(...arguments);
    this.set('player', null);
  },
  register(player) {
    this.set('player', player);
  },
  isPlaying: reads('player.isPlaying'),
});
