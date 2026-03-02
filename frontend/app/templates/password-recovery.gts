import type { TOC } from '@ember/component/template-only';
import RouteTemplate from 'ember-route-template';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { t } from 'ember-intl';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { hash } from '@ember/helper';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { on } from '@ember/modifier';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import UiButton from 'brn/components/ui/button';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { Input } from '@ember/component';
import type PasswordRecoveryController from 'brn/controllers/password-recovery';

interface Signature {
  Args: {
    controller: PasswordRecoveryController;
  };
}

const tpl: TOC<Signature> = <template>
    {{outlet}}

    {{#let
      (hash
        label="block mb-2 text-sm font-bold text-gray-700"
        input="appearance-none border-b-2 w-full text-sm leading-tight focus:outline-none focus:ring py-2"
      )
      as |style|
    }}

      <div class="w-full max-w-lg mx-auto">
        <form
          class="sm:px-16 sm:py-16 px-4 py-8 mb-6 bg-white rounded shadow-lg"
          {{on "submit" @controller.onSubmit}}
        >
          <div class="mb-4">
            <label for="email-input" class={{style.label}}>{{t
                "registration_form.email"
              }}</label>
            <Input
              id="email-input"
              class={{style.input}}
              placeholder={{t "registration_form.email"}}
              @value={{@controller.email}}
            />
          </div>

          <div class="flex mb-4">
            <UiButton
              @title={{t "password_reset_form.send_recovery_email"}}
              @type="button"
              @isLoading={{@controller.isSubmitting}}
              class="w-full pl-2 pr-2"
              {{on "click" @controller.sendRecoveryLink}}
            />
          </div>

          {{#if @controller.enableRecoveryCodeProcessing}}

            <div class="mb-4">
              <label for="recovery-code-input" class={{style.label}}>{{t
                  "password_reset_form.recovery_code"
                }}</label>
              <Input
                id="recovery-code-input"
                class={{style.input}}
                placeholder={{t "password_reset_form.recovery_code"}}
                @value={{@controller.code}}
              />
            </div>

            <div class="mb-4">
              <label for="new-password-input" class={{style.label}}>{{t
                  "password_reset_form.new_password"
                }}</label>
              <Input
                id="new-password-input"
                class={{style.input}}
                placeholder={{t "password_reset_form.new_password"}}
                @value={{@controller.newPassword}}
              />
            </div>

            <div class="flex mb-4">
              <UiButton
                @title={{t "password_reset_form.update_password"}}
                @type="button"
                @isLoading={{@controller.isSubmitting}}
                class="w-full pl-2 pr-2"
                {{on "click" @controller.changePassword}}
              />
            </div>

          {{/if}}

          {{#if @controller.error}}
            <div class="flex mb-4">
              <p class="mt-2 text-xs text-red-500">{{@controller.error}}</p>
            </div>
          {{/if}}

        </form>
      </div>

    {{/let}}
  </template>;

export default RouteTemplate(tpl);
