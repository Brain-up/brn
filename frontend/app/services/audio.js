import Service from '@ember/service';

export default Service.extend({
  init() {
    this._super(...arguments);
    this.set('player', null);
  },
  register(player) {
    this.set('player', player);
  },
});
