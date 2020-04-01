import { timeout } from 'ember-concurrency';
import Ember from 'ember';

export default async function customTimeout(ms = 16) {
  const delay = Ember.testing ? 1 : ms;
  return await timeout(delay);
}
