import Component from '@glimmer/component';
import { service } from '@ember/service';
import { action } from '@ember/object';
import { tracked } from '@glimmer/tracking';
import Session from 'ember-simple-auth/services/session';
import UserDataService from 'brn/services/user-data';
import { LinkTo } from '@ember/routing';
import { on } from '@ember/modifier';
import { fn } from '@ember/helper';
import { hash } from '@ember/helper';
import { t } from 'ember-intl';
import { eq } from 'ember-truth-helpers';
import UiIconLogo from 'brn/components/ui/icon/logo';
import UiIconLogout from 'brn/components/ui/icon/logout';
import UiButton from 'brn/components/ui/button';
import GlobalTimer from 'brn/components/global-timer';

export default class HeaderComponent extends Component {
  @service('session') session!: Session;
  @service('user-data') userData!: UserDataService;

  @tracked isLoggingOut = false;

  get activeLocale() {
    return this.userData.activeLocale;
  }

  get avatarUrl() {
    return this.userData.avatarUrl;
  }

  get user() {
    return this.userData.userModel;
  }

  @action async logout() {
    this.isLoggingOut = true;
    try {
      await this.session.invalidate();
      window.location.reload();
    } finally {
      this.isLoggingOut = false;
    }
  }

  @action setLocale(localeName: string) {
    this.userData.setLocale(localeName);
  }

  @action closeMenu() {
    const menu = document.getElementById('other-menu');

    if (menu) {
      const input = menu.querySelector('input');

      input && (input.checked = false);
    }
  }

  <template>
    <div
      class="
        header relative z-20
        {{if this.session.isAuthenticated "bg-purple-primary"}}"
      ...attributes
    >
      <nav class="justify-evenly md:p-4 flex flex-wrap p-2 pt-4">
        <div class="md:mr-4 z-10 flex flex-shrink text-white">
          <LinkTo
            @route="index"
            data-test-logo
            class="logo-text text-xl tracking-tight text-gray-700"
          >
            <UiIconLogo
              @fill={{if this.session.isAuthenticated "white" "black"}}
            />
          </LinkTo>
        </div>
        <div class="nav-links flex items-center w-auto">
          <div
            class="text-s sm:text-xs flex flex-wrap justify-center py-2
              {{if
                this.session.isAuthenticated
                "text-white"
                "text-purple-primary"
              }}"
          >
            {{#if this.session.isAuthenticated}}
              <LinkTo
                @route="groups"
                @query={{hash locale=this.activeLocale}}
                data-test-group-link
                class="hover:bg-white hover:text-purple-primary sm:px-4 sm:mr-4 block inline-block px-2 py-2 mr-0 font-semibold tracking-wider text-white uppercase rounded-full"
              >
                {{t "header.groups"}}
              </LinkTo>
              <LinkTo
                @route="profile.statistics"
                data-test-statistics-link
                class="hover:bg-white hover:text-purple-primary sm:px-4 inline-block px-2 py-2 mr-0 font-semibold tracking-wider text-white uppercase rounded-full"
              >
                {{t "header.statistics"}}
              </LinkTo>
            {{/if}}
            <LinkTo
              @route="description"
              class="inline-block px-2 sm:px-4 py-2 mr-0 font-bold tracking-wider uppercase rounded-full
                {{if
                  this.session.isAuthenticated
                  "hover:bg-white hover:text-purple-primary"
                  "hover:bg-purple-primary hover:text-white"
                }}"
            >
              {{t "header.about"}}
            </LinkTo>
            <a
              target="_blank"
              rel="noopener noreferrer"
              href="https://t.me/BrainUpUsers"
              title="Телеграм чат для пользователей"
              class="inline-block px-2 sm:px-4 py-2 mr-0 font-bold tracking-wider uppercase rounded-full
    
                {{if
                  this.session.isAuthenticated
                  "hover:bg-white hover:text-purple-primary"
                  "hover:bg-purple-primary hover:text-white"
                }}"
            >
              Telegram
            </a>
            <span
              class="sm:px-4 inline-block px-2 mr-0 font-semibold tracking-wider text-white uppercase rounded-full"
            >
              <button
                type="button"
                class="btn-press hover:text-opacity-50 py-2 bg-transparent rounded uppercase
                  {{if (eq this.activeLocale "ru-ru") "font-bold"}}
                  {{if
                    this.session.isAuthenticated
                    "text-white"
                    "text-purple-primary"
                  }}"
                {{on "click" (fn this.setLocale "ru")}}
              >
                RU
              </button>
              /
              <button
                type="button"
                class="btn-press inline-block hover:text-opacity-50 py-2 bg-transparent rounded uppercase
                  {{if (eq this.activeLocale "en-us") "font-bold"}}
                  {{if
                    this.session.isAuthenticated
                    "text-white"
                    "text-purple-primary"
                  }}"
                {{on "click" (fn this.setLocale "en")}}
              >
                EN
              </button>
            </span>
            <div id="other-menu" class="dropdown relative rounded-full">
              <button
                class="btn-press sm:px-4 sm:mr-4 inline-block px-2 py-2 mr-0 font-bold tracking-wider uppercase rounded-full"
                type="button"
              >{{t "header.more"}}</button>
              <input type="checkbox" />
              <div class="sm:text-base bottom absolute z-20 py-2 text-xs rounded">
                <a
                  target="_blank"
                  href="https://opencollective.com/brainup"
                  rel="noopener noreferrer"
                >{{t "header.donate"}}</a>
                <a
                  target="_blank"
                  href="https://github.com/Brain-up/brn"
                  rel="noopener noreferrer"
                >{{t "header.github"}}</a>
                <LinkTo @route="contact" {{on "click" this.closeMenu}}>{{t
                    "header.contact"
                }}</LinkTo>
                <LinkTo @route="specialists" {{on "click" this.closeMenu}}>{{t
                    "header.specialists"
                  }}</LinkTo>
                <LinkTo @route="contributors" {{on "click" this.closeMenu}}>{{t
                    "header.contributors"
                  }}</LinkTo>
                <LinkTo @route="used-resources" {{on "click" this.closeMenu}}>{{t
                    "header.used_resources"
                  }}</LinkTo>
              </div>
            </div>
          </div>
        </div>
        {{#if this.session.isAuthenticated}}
          <div class="flex items-center justify-end ml-6">
            <GlobalTimer />
    
            <LinkTo
              @route="profile"
              class="hover:text-opacity-50 text-md focus:underline flex items-center font-medium text-white transition duration-150 ease-in-out"
            >
              <div
                class="sm:mr-3 w-10 h-10 mr-2 border border-gray-400 rounded-full"
              >
                <img
                  src="{{this.avatarUrl}}"
                  alt="user avatar"
                  class="object-cover h-full rounded-full"
                />
              </div>
              <div
                class="font-small md:mr-4 text-xss sm:text-xs mr-3 leading-5 uppercase"
              >
                <p>
                  {{this.user.firstName}}
                </p>
                <p>
                  {{this.user.lastName}}
                </p>
              </div>
            </LinkTo>
    
            <button
              data-test-logout-button
              type="button"
              disabled={{this.isLoggingOut}}
              class="btn-press hover:text-opacity-50 text-xss sm:text-xs inline-block mt-0 mr-2 leading-none text-white border-0 {{if this.isLoggingOut "opacity-50 cursor-not-allowed"}}"
              {{on "click" this.logout}}
            >
              {{#if this.isLoggingOut}}
                <span class="btn-spinner" aria-hidden="true"></span>
              {{else}}
                <UiIconLogout />
              {{/if}}
            </button>
          </div>
        {{else}}
          <div class="relative z-10 flex justify-end px-16">
            <div>
              <UiButton
                data-test-registration-form
                class="flex items-center w-full px-16"
                @kind="outline"
                @route="registration"
                @title={{t "login_form.registration"}}
              />
            </div>
          </div>
        {{/if}}
      </nav>
    </div>
  </template>
}
