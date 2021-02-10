import Route from '@ember/routing/route';

export default class GroupSeriesSubgroupRoute extends Route {
  model({subgroup_id}: { subgroup_id: string }) {
    return this.store.query('exercise', {
      subGroupId: subgroup_id
    });
  }
}
