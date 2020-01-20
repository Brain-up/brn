import { timeout } from 'ember-concurrency';
import ENV from 'brn/config/environment';

export default async function customTimeout(ms) {
  const delay = ENV.environment !== 'test' ? ms : 1;
  return await timeout(delay);
}
