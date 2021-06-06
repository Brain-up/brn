import { module, test } from 'qunit';
import { setupTest } from 'ember-qunit';
import { isArray } from '@ember/array';
import Service from '@ember/service';

module('Unit | Service | persons', function(hooks) {
  setupTest(hooks);

  test('it exists', function(assert) {
    let service = this.owner.lookup('service:persons');
    assert.ok(service);
  });

  test('has persons data', function(assert) {
    let service = this.owner.lookup('service:persons');
    assert.ok(
      isArray(service.persons.doctors) && isArray(service.persons.teamMembers)
    );
  });

  test('has teamMembers data', function(assert) {
    let service = this.owner.lookup('service:persons');
    assert.ok(
      service.persons.teamMembers.length === 37
    );
  });

  test('has teamMembers image', function(assert) {
    let service = this.owner.lookup('service:persons');
    assert.ok(
      service.persons.teamMembers[0].img.includes('/content/pages/description/team-members')
    );
  });

  test('has doctors data', function(assert) {
    let service = this.owner.lookup('service:persons');
    assert.ok(
      isArray(service.doctors) && service.doctors.length === 9 && service.doctors[0].img && service.doctors[0].name && service.doctors[0].bio
    );
  });

  test('has doctors data in ru language', function(assert) {
    this.owner.register(
      'service:intl',
      class MockService extends Service {
        get locale() {
          return ['ru-ru']
        }
      }
    );
    let service = this.owner.lookup('service:persons');
    assert.ok(
      service.doctors[0].name === 'Королева Инна Васильевна'
    );
  });

  test('has doctors data in en language', function(assert) {
    this.owner.register(
      'service:intl',
      class MockService extends Service {
        get locale() {
          return ['en-us']
        }
      }
    );
    let service = this.owner.lookup('service:persons');
    assert.ok(
      service.doctors[0].name === 'Inna Koroleva'
    );
  });

  test('has doctors image', function(assert) {
    let service = this.owner.lookup('service:persons');
    assert.ok(
      service.doctors[0].img.includes('/content/pages/description/doctors')
    );
  });
});
