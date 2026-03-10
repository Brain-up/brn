import Route from '@ember/routing/route';
import { service } from '@ember/service';
import { isTesting } from '@embroider/macros';
import translationsForRuRu from 'virtual:ember-intl/translations/ru-ru';
import translationsForEnUs from 'virtual:ember-intl/translations/en-us';
import isServerError from 'brn/utils/is-server-error';

export default class ApplicationRoute extends Route {
  @service('session') session;
  @service('intl') intl;
  @service('router') router;
  @service('network') network;
  @service('tasks-manager') tasksManager;

  async beforeModel() {
    await this.session.setup();

    if (this.session.isAuthenticated) {
      try {
        await Promise.all([
          this.network.loadCurrentUser(),
          this.tasksManager.loadTodayCompletedExercises(),
        ]);
      } catch (e) {
        // Re-throw server/network errors so the error template is shown
        if (isServerError(e)) {
          throw e;
        }
        // Other errors (e.g. 401) are handled by loadCurrentUser (redirects to login)
      }
    }

    this.intl.addTranslations('ru-ru', translationsForRuRu);
    this.intl.addTranslations('en-us', translationsForEnUs);

    const navigatorLanguage = navigator.languages.filter(el => el.includes('-')).map(el => el.toLowerCase())[0];
    const rawLocale = localStorage.getItem('locale') || navigatorLanguage;
    const locale = rawLocale === 'ru-ru' ? 'ru-ru' : 'en-us';
    this.intl.setLocale([locale]);
  }

  redirect(_/* : unknown */, { to }/*: Transition*/) {
    if (isTesting()) {
      // skip testing behaviour for now
      return;
    }
    if (['user-agreement', 'description', 'contributors'].includes(to.name)) {
      return;
    }
    if (!this.session.isAuthenticated) {
      this.router.replaceWith('index');
    }
  }
}
