import Controller from '@ember/controller';
import { inject } from '@ember/service';
import { computed } from '@ember/object';

export default Controller.extend({
  router: inject(),
  headerAndNavShown: computed('router.currentURL', function() {
    console.log(
      this.router.currentURL,
      this.router.currentURL.includes('task'),
    );
    return (
      this.router.currentURL.includes('task') ||
      this.router.currentURL.includes('loading')
    );
  }),
});
