import Route from '@ember/routing/route';

export default Route.extend({
  async afterModel(model) {
    if (!model.canInteract) {
      this.transitionTo('not-accessable');
    }
  },
});
