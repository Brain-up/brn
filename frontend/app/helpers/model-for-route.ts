import Helper from '@ember/component/helper';
import { getOwner } from '@ember/application';
import type Route from '@ember/routing/route';

interface ModelForRouteSignature {
  Args: {
    Positional: [string];
  };
  Return: unknown;
}

export default class ModelForRoute extends Helper<ModelForRouteSignature> {
  compute([routeName]: [string]): unknown {
    const appRoute = getOwner(this)!.lookup('route:application') as Route;
    const model = appRoute.modelFor(routeName) as Record<string, unknown>;
    // Group route returns a composite { group, series } model;
    // extract the group record for compatibility.
    // Use 'in' check to avoid triggering WarpDrive's strict proxy on a bare record.
    if (routeName === 'group' && model && 'group' in model && 'series' in model) {
      return model.group;
    }
    return model;
  }
}
