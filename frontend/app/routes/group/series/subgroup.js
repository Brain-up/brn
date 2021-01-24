import Route from '@ember/routing/route';

export default class GroupSeriesSubgroupRoute extends Route {
  model({subgroup_id}) {
    return this.store.query('exercise', {
      subGroupId: subgroup_id
    });
  }
}
