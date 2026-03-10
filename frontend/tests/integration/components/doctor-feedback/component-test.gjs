import { module, test } from 'qunit';
import { setupIntl } from 'ember-intl/test-support';
import { setupRenderingTest } from 'ember-qunit';
import { render } from '@ember/test-helpers';
import DoctorFeedback from 'brn/components/doctor-feedback';

module('Integration | Component | doctor-feedback', function (hooks) {
  setupRenderingTest(hooks);
  setupIntl(hooks, 'en-us');

  test('it renders the feedback section', async function (assert) {
    await render(<template><DoctorFeedback /></template>);

    assert.dom('[data-test-doctor-feedback]').exists();
    assert.dom('[data-test-doctor-feedback] h2').hasText('t:doctor_feedback.title');
    assert.dom('[data-test-doctor-feedback] .italic').exists('renders feedback text in italic');
    assert.dom('[data-test-doctor-feedback] .italic').hasText('t:doctor_feedback.text');
  });

  test('it renders doctor name and credentials', async function (assert) {
    await render(<template><DoctorFeedback /></template>);

    assert.dom('[data-test-doctor-feedback]').includesText('t:doctor_feedback.name');
    assert.dom('[data-test-doctor-feedback]').includesText('t:doctor_feedback.credentials');
  });
});
