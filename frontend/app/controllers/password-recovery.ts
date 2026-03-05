import Controller from '@ember/controller';
import { action } from '@ember/object';
import { getOwner } from '@ember/application';
import type FirebaseAuthenticator from 'brn/authenticators/firebase';
import { tracked } from '@glimmer/tracking';
import type Router from '@ember/routing/router-service';
import { inject as service } from '@ember/service';
import UserDataService from 'brn/services/user-data';

export default class PasswordRecoveryController extends Controller {
  @service('router') router!: Router;
  @service('user-data') userData!: UserDataService;

  @tracked isConfirmationEmailSend = false;

  @tracked email = this.userData?.userModel?.email ?? '';
  @tracked code = '';
  @tracked newPassword = '';
  @tracked error = '';
  @tracked isSubmitting = false;

  enableRecoveryCodeProcessing = false;

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
    this.isSubmitting = true;
    try {
      await this.firebase.resetPassword(this.email.trim());
      this.router.transitionTo('login');
    } catch (e) {
      this.error = e.message;
    } finally {
      this.isSubmitting = false;
    }
  }

  @action
  async changePassword() {
    this.resetErrors();
    this.isSubmitting = true;
    try {
      await this.firebase.confirmPasswordReset(this.code, this.newPassword);
    } catch (e) {
      this.error = e.message;
    } finally {
      this.isSubmitting = false;
    }
  }

  @action
  onSubmit(e: Event) {
    e.preventDefault();
    e.stopPropagation();
  }
}
