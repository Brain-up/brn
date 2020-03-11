import Route from '@ember/routing/route';
import { inject as service } from '@ember/service';
import Ember from 'ember';
export default class ApplicationRoute extends Route {
	@service('session') session;
	redirect() {
		if (Ember.testing) {
			// skip testing bahavour for now
			return;
		}
		if (!this.session.isAuthenticated) {
			this.replaceWith('login');
		}
	}
}
