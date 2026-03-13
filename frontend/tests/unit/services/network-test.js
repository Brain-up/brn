import { module, test } from 'qunit';
import { setupTest } from 'ember-qunit';
import { setupMSW } from '../../helpers/msw';
import { getCloudBaseUrl, setCloudBaseUrl } from 'brn/utils/file-url';

module('Unit | Service | network', function (hooks) {
  setupTest(hooks);
  setupMSW(hooks);

  // Replace this with your real tests.
  test('it exists', function (assert) {
    const service = this.owner.lookup('service:network');
    assert.ok(service);
  });

  hooks.afterEach(function () {
    // Reset cloud base URL after each test
    setCloudBaseUrl('');
  });

  test('cloudUrl fetches base file URL from API', async function (assert) {
    window.server.get('cloud/baseFileUrl', () => ({
      data: 'https://brnup.s3.eu-north-1.amazonaws.com',
    }));

    const network = this.owner.lookup('service:network');
    const url = await network.cloudUrl();

    assert.strictEqual(url, 'https://brnup.s3.eu-north-1.amazonaws.com');
  });

  test('loadCloudUrl sets cloud base URL via setCloudBaseUrl', async function (assert) {
    window.server.get('cloud/baseFileUrl', () => ({
      data: 'https://brnup.s3.eu-north-1.amazonaws.com',
    }));

    const network = this.owner.lookup('service:network');
    await network.loadCloudUrl();

    assert.strictEqual(
      getCloudBaseUrl(),
      'https://brnup.s3.eu-north-1.amazonaws.com',
    );
  });

  test('loadCloudUrl does not set cloud base URL when API returns null', async function (assert) {
    window.server.get('cloud/baseFileUrl', () => ({
      data: null,
    }));

    const network = this.owner.lookup('service:network');
    // Pre-set a value to confirm it is NOT overwritten
    setCloudBaseUrl('https://existing.example.com');
    await network.loadCloudUrl();

    // Since API returned null, setCloudBaseUrl should not be called,
    // so the existing value remains
    assert.strictEqual(getCloudBaseUrl(), 'https://existing.example.com');
  });

  test('loadCloudUrl does not set cloud base URL when API returns empty string', async function (assert) {
    window.server.get('cloud/baseFileUrl', () => ({
      data: '',
    }));

    const network = this.owner.lookup('service:network');
    setCloudBaseUrl('https://existing.example.com');
    await network.loadCloudUrl();

    // Empty string is falsy, so setCloudBaseUrl should not be called
    assert.strictEqual(getCloudBaseUrl(), 'https://existing.example.com');
  });

  test('loadCloudUrl strips trailing slash from cloud URL', async function (assert) {
    window.server.get('cloud/baseFileUrl', () => ({
      data: 'https://brnup.s3.eu-north-1.amazonaws.com/',
    }));

    const network = this.owner.lookup('service:network');
    await network.loadCloudUrl();

    assert.strictEqual(
      getCloudBaseUrl(),
      'https://brnup.s3.eu-north-1.amazonaws.com',
    );
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
