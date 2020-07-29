import Application from 'brn/app';
import config from 'brn/config/environment';
import { setApplication } from '@ember/test-helpers';
import 'qunit-dom';
import { start } from 'ember-qunit';

setApplication(Application.create(config.APP));

start();
