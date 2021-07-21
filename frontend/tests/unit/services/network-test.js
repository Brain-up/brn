import { module, test } from 'qunit';
import { setupTest } from 'ember-qunit';
import setupMirage from 'ember-cli-mirage/test-support/setup-mirage';

module('Unit | Service | network', function (hooks) {
  setupTest(hooks);
  setupMirage(hooks);

  // Replace this with your real tests.
  test('it exists', function (assert) {
    const service = this.owner.lookup('service:network');
    assert.ok(service);
  });

  test('it formats date for request', async function (assert) {
    assert.expect(2);

    server.get('/statistics/study/week', function (schema, request) {
      const { from, to } = request.queryParams;
      assert.equal(from, '2021-01-01', 'date from is correct');
      assert.equal(to, '2021-12-31', 'date to is correct');
      return {};
    });

    const service = this.owner.lookup('service:network');

    const toDate = new Date('2021-12-31');
    const fromDate = new Date('2021-01-01');
    service.getUserStatisticsByWeek(fromDate, toDate);
  });
});
