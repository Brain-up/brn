import { timeout } from 'ember-concurrency';
import Ember from 'ember';

export default async function customTimeout(ms) {
  const delay = Ember.testing ? 1 : ms;
  return await timeout(delay);
}
