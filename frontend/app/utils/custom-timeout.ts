import { timeout } from 'ember-concurrency';
import { isTesting } from '@embroider/macros';

export default async function customTimeout(ms = 16): Promise<unknown> {
  const delay = isTesting() ? 1 : ms;
  return await timeout(delay);
}
