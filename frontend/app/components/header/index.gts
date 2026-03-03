import './index.css';
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
import UiButton from 'brn/components/ui/button';
import GlobalTimer from 'brn/components/global-timer';
import LoadingSpinner from 'brn/components/loading-spinner';

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
        c-header header relative z-20
        {{if this.session.isAuthenticated "bg-purple-primary"}}"
      ...attributes
    >
      {{#if this.session.isAuthenticated}}
        <nav class="sm:px-4 sm:py-3 flex items-center px-2 py-2">
          <LinkTo
            @route="index"
            data-test-logo
            class="md:inline-flex items-center flex-shrink-0 hidden mr-4 text-xl tracking-tight text-gray-700"
          >
            <UiIconLogo @fill="white" />
          </LinkTo>
          <div class="nav-links flex items-center flex-1 min-w-0">
            <div class="sm:text-sm flex items-center text-xs text-white whitespace-nowrap">
              <LinkTo
                @route="groups"
                @query={{hash locale=this.activeLocale}}
                data-test-group-link
                class="hover:bg-white hover:text-purple-primary sm:inline-block sm:px-4 hidden px-2 py-2 font-semibold tracking-wider uppercase rounded-full"
              >
                {{t "header.groups"}}
              </LinkTo>
              <LinkTo
                @route="profile.statistics"
                data-test-statistics-link
                class="hover:bg-white hover:text-purple-primary sm:inline-block sm:px-4 hidden px-2 py-2 font-semibold tracking-wider uppercase rounded-full"
              >
                {{t "header.statistics"}}
              </LinkTo>
              <LinkTo
                @route="description"
                class="hover:bg-white hover:text-purple-primary lg:inline-block lg:px-4 hidden px-2 py-2 font-bold tracking-wider uppercase rounded-full"
              >
                {{t "header.about"}}
              </LinkTo>
              <a
                target="_blank"
                rel="noopener noreferrer"
                href="https://t.me/BrainUpUsers"
                title="Телеграм чат для пользователей"
                class="hover:bg-white hover:text-purple-primary xl:inline-block xl:px-4 hidden px-2 py-2 font-bold tracking-wider uppercase rounded-full"
              >
                Telegram
              </a>
              <span class="md:inline-flex md:px-2 items-center flex-shrink-0 hidden font-semibold tracking-wider text-white uppercase">
                <button
                  type="button"
                  class="btn-press hover:text-opacity-50 py-2 text-white bg-transparent rounded uppercase {{if (eq this.activeLocale 'ru-ru') 'font-bold'}}"
                  {{on "click" (fn this.setLocale "ru")}}
                >RU</button>
                <span class="mx-1">/</span>
                <button
                  type="button"
                  class="btn-press hover:text-opacity-50 py-2 text-white bg-transparent rounded uppercase {{if (eq this.activeLocale 'en-us') 'font-bold'}}"
                  {{on "click" (fn this.setLocale "en")}}
                >EN</button>
              </span>
              <div id="other-menu" class="dropdown relative rounded-full">
                <button
                  class="btn-press sm:px-4 inline-block px-2 py-2 font-bold tracking-wider uppercase rounded-full"
                  type="button"
                >{{t "header.more"}}</button>
                <input type="checkbox" />
                <div class="sm:text-base bottom absolute right-0 z-20 py-2 text-xs rounded">
                  <LinkTo class="sm:hidden" @route="groups" @query={{hash locale=this.activeLocale}} {{on "click" this.closeMenu}}>{{t "header.groups"}}</LinkTo>
                  <LinkTo class="sm:hidden" @route="profile.statistics" {{on "click" this.closeMenu}}>{{t "header.statistics"}}</LinkTo>
                  <div class="md:hidden flex items-center px-5 py-1">
                    <button
                      type="button"
                      class="btn-press hover:opacity-50 py-1 text-white bg-transparent uppercase {{if (eq this.activeLocale 'ru-ru') 'font-bold'}}"
                      {{on "click" (fn this.setLocale "ru")}}
                    >RU</button>
                    <span class="mx-1 text-white">/</span>
                    <button
                      type="button"
                      class="btn-press hover:opacity-50 py-1 text-white bg-transparent uppercase {{if (eq this.activeLocale 'en-us') 'font-bold'}}"
                      {{on "click" (fn this.setLocale "en")}}
                    >EN</button>
                  </div>
                  <LinkTo class="lg:hidden" @route="description" {{on "click" this.closeMenu}}>{{t "header.about"}}</LinkTo>
                  <a class="xl:hidden" target="_blank" href="https://t.me/BrainUpUsers" rel="noopener noreferrer" {{on "click" this.closeMenu}}>Telegram</a>
                  <a target="_blank" href="https://opencollective.com/brainup" rel="noopener noreferrer">{{t "header.donate"}}</a>
                  <a target="_blank" href="https://github.com/Brain-up/brn" rel="noopener noreferrer">{{t "header.github"}}</a>
                  <LinkTo @route="contact" {{on "click" this.closeMenu}}>{{t "header.contact"}}</LinkTo>
                  <LinkTo @route="specialists" {{on "click" this.closeMenu}}>{{t "header.specialists"}}</LinkTo>
                  <LinkTo @route="contributors" {{on "click" this.closeMenu}}>{{t "header.contributors"}}</LinkTo>
                  <LinkTo @route="used-resources" {{on "click" this.closeMenu}}>{{t "header.used_resources"}}</LinkTo>
                </div>
              </div>
            </div>
          </div>
          <div class="sm:ml-4 flex items-center flex-shrink-0 ml-1">
            <LinkTo @route="profile.statistics" class="flex-shrink-0">
              <GlobalTimer />
            </LinkTo>
            <LinkTo
              @route="profile"
              class="hover:text-opacity-50 focus:underline flex items-center font-medium text-white transition duration-150 ease-in-out"
            >
              <div class="sm:w-10 sm:h-10 sm:mr-2 w-8 h-8 mr-1 border border-gray-400 rounded-full">
                <img
                  src="{{this.avatarUrl}}"
                  alt="user avatar"
                  class="object-cover h-full rounded-full"
                />
              </div>
              <div class="md:mr-4 sm:block hidden mr-2 text-xs leading-5 uppercase">
                <p>{{this.user.firstName}}</p>
                <p>{{this.user.lastName}}</p>
              </div>
            </LinkTo>
            <button
              data-test-logout-button
              type="button"
              disabled={{this.isLoggingOut}}
              class="btn-press hover:text-opacity-50 text-xs inline-block leading-none text-white border-0 {{if this.isLoggingOut 'opacity-50 cursor-not-allowed'}}"
              {{on "click" this.logout}}
            >
              {{#if this.isLoggingOut}}
                <LoadingSpinner />
              {{else}}
                {{t "header.sign_out"}}
              {{/if}}
            </button>
          </div>
        </nav>
      {{else}}
        <nav class="justify-evenly md:p-4 flex flex-wrap items-center p-2 pt-4">
          <div class="md:mr-4 z-10 flex flex-shrink-0">
            <LinkTo
              @route="index"
              data-test-logo
              class="logo-text text-xl tracking-tight text-gray-700"
            >
              <UiIconLogo @fill="black" />
            </LinkTo>
          </div>
          <div class="nav-links flex items-center w-auto">
            <div class="text-purple-primary sm:text-sm flex flex-wrap items-center justify-center py-2 text-xs">
              <LinkTo
                @route="description"
                class="hover:bg-purple-primary hover:text-white sm:px-4 inline-block px-2 py-2 font-bold tracking-wider uppercase rounded-full"
              >
                {{t "header.about"}}
              </LinkTo>
              <a
                target="_blank"
                rel="noopener noreferrer"
                href="https://t.me/BrainUpUsers"
                title="Телеграм чат для пользователей"
                class="hover:bg-purple-primary hover:text-white sm:px-4 inline-block px-2 py-2 font-bold tracking-wider uppercase rounded-full"
              >
                Telegram
              </a>
              <span class="sm:px-4 inline-block px-2 font-semibold tracking-wider uppercase">
                <button
                  type="button"
                  class="btn-press hover:text-opacity-50 py-2 text-purple-primary bg-transparent rounded uppercase {{if (eq this.activeLocale 'ru-ru') 'font-bold'}}"
                  {{on "click" (fn this.setLocale "ru")}}
                >RU</button>
                /
                <button
                  type="button"
                  class="btn-press hover:text-opacity-50 py-2 text-purple-primary bg-transparent rounded uppercase {{if (eq this.activeLocale 'en-us') 'font-bold'}}"
                  {{on "click" (fn this.setLocale "en")}}
                >EN</button>
              </span>
              <div id="other-menu" class="dropdown relative rounded-full">
                <button
                  class="btn-press sm:px-4 inline-block px-2 py-2 font-bold tracking-wider uppercase rounded-full"
                  type="button"
                >{{t "header.more"}}</button>
                <input type="checkbox" />
                <div class="sm:text-base bottom absolute right-0 z-20 py-2 text-xs rounded">
                  <a target="_blank" href="https://opencollective.com/brainup" rel="noopener noreferrer">{{t "header.donate"}}</a>
                  <a target="_blank" href="https://github.com/Brain-up/brn" rel="noopener noreferrer">{{t "header.github"}}</a>
                  <LinkTo @route="contact" {{on "click" this.closeMenu}}>{{t "header.contact"}}</LinkTo>
                  <LinkTo @route="specialists" {{on "click" this.closeMenu}}>{{t "header.specialists"}}</LinkTo>
                  <LinkTo @route="contributors" {{on "click" this.closeMenu}}>{{t "header.contributors"}}</LinkTo>
                  <LinkTo @route="used-resources" {{on "click" this.closeMenu}}>{{t "header.used_resources"}}</LinkTo>
                </div>
              </div>
            </div>
          </div>
          <div class="relative z-10 flex justify-end px-4">
            <div>
              <UiButton
                data-test-registration-form
                class="sm:px-16 flex items-center w-full px-6"
                @kind="outline"
                @route="registration"
                @title={{t "login_form.registration"}}
              />
            </div>
          </div>
        </nav>
      {{/if}}
    </div>
  </template>
}
