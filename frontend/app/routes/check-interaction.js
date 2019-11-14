import Route from '@ember/routing/route';

export default Route.extend({
  async afterModel(model, { to }) {
    const parentContent = model.parent.content;
    const parentModelName = parentContent.constructor.modelName;
    const parentRouteId = to.parent.params[`${parentModelName}_id`];
    const wrongParent =
      parentRouteId &&
      parentContent.id !== to.parent.params[`${parentModelName}_id`];

    if (!model.canInteract || (parentModelName !== 'group' && wrongParent)) {
      this.transitionTo('not-accessable');
    }
  },
});
