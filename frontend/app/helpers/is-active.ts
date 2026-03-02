import { helper } from '@ember/component/helper';
import { getOwner } from '@ember/owner';

export default helper(function isActive(this: object, params: string[]) {
  const routeName = params[0];
  const owner = getOwner(this);
  if (!owner) return false;
  const router = owner.lookup('service:router') as any;
  return router?.isActive?.(routeName) ?? false;
});
