import Route from '@ember/routing/route';
import { inject as service } from '@ember/service';
import type { Store } from '@warp-drive-mirror/core';

export default class GroupSeriesSubgroupRoute extends Route {
  @service('store') store!: Store;
  model({ subgroup_id }: { subgroup_id: string }) {
    return this.store.query('exercise', {
      subGroupId: subgroup_id,
    });
  }
}
