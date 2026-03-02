import Application from 'brn/app';
import config from 'brn/config/environment';
import '@warp-drive/ember/install';
import * as QUnit from 'qunit';
import { setApplication } from '@ember/test-helpers';
import { setup } from 'qunit-dom';
import { start as qunitStart, setupEmberOnerrorValidation } from 'ember-qunit';
import DefaultAdapter from 'ember-cli-page-object/adapters/rfc268';
import { setAdapter } from 'ember-cli-page-object/adapters';

export function start() {
  setAdapter(new DefaultAdapter());
  setApplication(Application.create(config.APP));
  setup(QUnit.assert);
  setupEmberOnerrorValidation();
  qunitStart();
}
