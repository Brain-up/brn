import Controller from '@ember/controller';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { inject as service } from '@ember/service';
import Router from '@ember/routing/router-service';

export default class ApplicationController extends Controller {
  @service router!: Router;
  get headerAndNavShown() {
    return (
      this.router.currentURL.includes('task') ||
      this.router.currentURL.includes('loading')
    );
  }
}
