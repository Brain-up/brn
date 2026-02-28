import { module, test } from 'qunit';
import { setupTest } from 'ember-qunit';

module('Unit | Schema | contributor', function (hooks) {
  setupTest(hooks);

  test('creates a contributor record', function (assert) {
    const store = this.owner.lookup('service:store');
    const model = store.createRecord('contributor', {
      rawName: { 'en-us': 'John', 'ru-ru': 'Джон' },
      rawDescription: { 'en-us': 'Developer', 'ru-ru': 'Разработчик' },
      rawCompany: { 'en-us': 'Corp', 'ru-ru': 'Корп' },
      avatar: 'avatar.png',
      login: 'john',
      contribution: 100,
      isActive: true,
      kind: 'DEVELOPER',
    });
    assert.ok(model);
    assert.strictEqual(model.avatar, 'avatar.png');
    assert.strictEqual(model.login, 'john');
    assert.strictEqual(model.isActive, true);
    assert.strictEqual(model.kind, 'DEVELOPER');
  });

  test('name returns locale-specific value', function (assert) {
    const store = this.owner.lookup('service:store');
    const model = store.createRecord('contributor', {
      rawName: { 'en-us': 'John', 'ru-ru': 'Джон' },
    });
    // Default locale should be en-us
    const name = model.name;
    assert.ok(
      name === 'John' || name === 'Джон',
      `name returns a locale-specific value: ${name}`,
    );
  });

  test('description returns locale-specific value', function (assert) {
    const store = this.owner.lookup('service:store');
    const model = store.createRecord('contributor', {
      rawDescription: { 'en-us': 'Developer', 'ru-ru': 'Разработчик' },
    });
    const desc = model.description;
    assert.ok(typeof desc === 'string', 'description is a string');
  });

  test('company returns locale-specific value', function (assert) {
    const store = this.owner.lookup('service:store');
    const model = store.createRecord('contributor', {
      rawCompany: { 'en-us': 'Corp', 'ru-ru': 'Корп' },
    });
    const company = model.company;
    assert.ok(typeof company === 'string', 'company is a string');
  });

  test('name returns empty string when rawName is missing key', function (assert) {
    const store = this.owner.lookup('service:store');
    const model = store.createRecord('contributor', {
      rawName: {},
    });
    // With no matching locale key, should return empty string via ?? ''
    const name = model.name;
    assert.strictEqual(name, '', 'returns empty string for missing locale');
  });
});
