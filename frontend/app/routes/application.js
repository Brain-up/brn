import Route from '@ember/routing/route';
import { service } from '@ember/service';
import { isTesting } from '@embroider/macros';
import translationsForRuRu from 'virtual:ember-intl/translations/ru-ru';
import translationsForEnUs from 'virtual:ember-intl/translations/en-us';

export default class ApplicationRoute extends Route {
  @service('session') session;
  @service('intl') intl;
  @service('router') router;

  async beforeModel() {
    await this.session.setup();

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
