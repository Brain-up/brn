import { timeout } from 'ember-concurrency';
import Ember from 'ember';

export default async function customTimeout(ms = 16, useMsValueForTest = false) {
  const delay = Ember.testing ? ( useMsValueForTest ? ms : 1): ms;
  return await timeout(delay);
}
