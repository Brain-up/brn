import Route from '@ember/routing/route';

export default Route.extend({
  async afterModel(model) {
    if (!model.canInteract) {
      const routeName = model.parent.content.constructor.name.toLowerCase();
      this.transitionTo(`${routeName}`, model.parent);
    }
  },
});
