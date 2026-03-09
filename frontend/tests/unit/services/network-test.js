import { module, test } from 'qunit';
import { setupTest } from 'ember-qunit';
import { setupMSW } from '../../helpers/msw';

module('Unit | Service | network', function (hooks) {
  setupTest(hooks);
  setupMSW(hooks);

  // Replace this with your real tests.
  test('it exists', function (assert) {
    const service = this.owner.lookup('service:network');
    assert.ok(service);
  });

  test('loadCurrentUser sets userData.userModel', async function (assert) {
    window.server.get('users/current', () => ({
      data: [
        {
          id: '42',
          name: 'Test User',
          email: 'test@example.com',
          bornYear: 1990,
          gender: 'MALE',
          active: true,
          avatar: '3',
          roles: ['ROLE_USER'],
        },
      ],
      errors: [],
      meta: [],
    }));

    const network = this.owner.lookup('service:network');
    const userData = this.owner.lookup('service:user-data');

    assert.strictEqual(userData.userModel, undefined, 'userModel is initially undefined');

    await network.loadCurrentUser();

    assert.ok(userData.userModel, 'userModel is set after loadCurrentUser');
    assert.strictEqual(userData.userModel.firstName, 'Test', 'firstName parsed from name');
    assert.strictEqual(userData.userModel.lastName, 'User', 'lastName parsed from name');
    assert.strictEqual(userData.userModel.email, 'test@example.com', 'email is set');
    assert.strictEqual(userData.userModel.avatar, '3', 'avatar is set');
    assert.strictEqual(userData.userModel.gender, 'MALE', 'gender is set');
    assert.strictEqual(userData.userModel.id, '42', 'id is set');
    assert.strictEqual(userData.userModel.initials, 'TU', 'initials computed correctly');
  });
});
