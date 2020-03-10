import OAuth2PasswordGrant from 'ember-simple-auth/authenticators/oauth2-password-grant';
import RSVP from 'rsvp';
import { inject as service } from '@ember/service';

export default OAuth2PasswordGrant.extend({
	network: service('network'),
	serverTokenEndpoint: 'brnlogin',
	makeRequest(url, data) {
		return new RSVP.Promise((resolve, reject) => {
		  this.network.postRequest(url, data).then((response) => {
			response.text().then((text) => {
			  try {
				let json = JSON.parse(text);
				if (!response.ok) {
				  response.responseJSON = json;
				  reject(response);
				} else {
				  resolve(json);
				}
			  } catch (SyntaxError) {
				response.responseText = text;
				reject(response);
			  }
			});
		  }).catch(reject);
		});
	  },
});