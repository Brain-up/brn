import OAuth2PasswordGrant from 'ember-simple-auth/authenticators/oauth2-password-grant';
import fetch from 'fetch';
import RSVP from 'rsvp';

export default OAuth2PasswordGrant.extend({
	serverTokenEndpoint: 'api/brnlogin',
	makeRequest(url, data, headers = {}) {

		headers['Content-Type'] = 'application/json';

		const options = {
		  body: JSON.stringify(data),
		  headers,
		  method: 'POST'
		};
	
		return new RSVP.Promise((resolve, reject) => {
		  fetch(url, options).then((response) => {
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