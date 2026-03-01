/* istanbul ignore file */

import OAuth2PasswordGrant from 'ember-simple-auth/authenticators/oauth2-password-grant';
import { inject as service } from '@ember/service';
import type NetworkService from 'brn/services/network';

export default class OAuth2Authenticator extends OAuth2PasswordGrant {
  @service declare network: NetworkService;

  serverTokenEndpoint = 'brnlogin';

  override async makeRequest(url: string, data: Record<string, string>): Promise<Record<string, unknown>> {
    const response = await this.network.postRequest(url, data) as Response;
    const text = await response.text();
    try {
      const json = JSON.parse(text) as Record<string, unknown>;
      if (!response.ok) {
        (response as Response & { responseJSON: Record<string, unknown> }).responseJSON = json;
        throw response;
      }
      return json;
    } catch (e) {
      if (e === response) {
        throw e;
      }
      (response as Response & { responseText: string }).responseText = text;
      throw response;
    }
  }
}
