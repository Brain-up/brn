import Controller from '@ember/controller';
import { action } from '@ember/object';
import { getOwner } from '@ember/application';
import type FirebaseAuthenticator from 'brn/authenticators/firebase';
import { tracked } from '@glimmer/tracking';

export default class PasswordRecoveryController extends Controller {
  @tracked isConfirmationEmailSend = false;

  @tracked email = '';
  @tracked code = '';
  @tracked newPassword = '';
  @tracked error = '';

  get firebase(): FirebaseAuthenticator {
    return getOwner(this).lookup(
      'authenticator:firebase',
    ) as FirebaseAuthenticator;
  }

  resetErrors() {
    if (this.error) {
      this.error = '';
    }
  }

  @action
  async sendRecoveryLink() {
    this.resetErrors();
    try {
      await this.firebase.resetPassword(this.email);
    } catch (e) {
      this.error = e.message;
    }
  }

  @action
  async changePassword() {
    this.resetErrors();
    try {
      await this.firebase.confirmPasswordReset(this.code, this.newPassword);
    } catch (e) {
      this.error = e.message;
    }
  }
}
