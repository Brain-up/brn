import Application from 'brn/app';
import config from 'brn/config/environment';
import * as QUnit from 'qunit';
import { setApplication } from '@ember/test-helpers';
import { setup } from 'qunit-dom';
import { start } from 'ember-qunit';
import DefaultAdapter from 'ember-cli-page-object/adapters/rfc268';
import { setAdapter } from 'ember-cli-page-object/adapters';

setAdapter(new DefaultAdapter());

setApplication(Application.create(config.APP));

setup(QUnit.assert);

start();
