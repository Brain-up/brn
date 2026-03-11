import Component from '@glimmer/component';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { action } from '@ember/object';
import { on } from '@ember/modifier';
import didInsert from '@ember/render-modifiers/modifiers/did-insert';
import { t } from 'ember-intl';

interface UiConfirmDialogSignature {
  Args: {
    message: string;
    onConfirm: () => void;
    onCancel: () => void;
    confirmLabel?: string;
    cancelLabel?: string;
    destructive?: boolean;
  };
  Element: HTMLDialogElement;
}

export default class UiConfirmDialogComponent extends Component<UiConfirmDialogSignature> {
  dialogElement: HTMLDialogElement | null = null;

  get confirmLabel(): string {
    return this.args.confirmLabel ?? '';
  }

  get cancelLabel(): string {
    return this.args.cancelLabel ?? '';
  }

  get confirmButtonClass(): string {
    const base =
      'btn-press min-h-[44px] px-5 py-2 text-sm font-medium text-white rounded-md';
    if (this.args.destructive) {
      return `${base} bg-red-600 hover:bg-red-700`;
    }
    return `${base} bg-indigo-600 hover:bg-indigo-700`;
  }

  @action
  onInsert(element: HTMLDialogElement) {
    this.dialogElement = element;
    element.showModal();
    element.addEventListener('close', this.onDialogClose);
  }

  @action
  onDialogClose() {
    if (this.dialogElement?.returnValue !== 'confirm') {
      this.args.onCancel();
    }
  }

  @action
  confirm() {
    this.dialogElement?.close('confirm');
    this.args.onConfirm();
  }

  @action
  cancel() {
    // Remove listener first to prevent async double-call from the native
    // 'close' event, then close the dialog and notify synchronously.
    this.dialogElement?.removeEventListener('close', this.onDialogClose);
    this.dialogElement?.close('cancel');
    this.args.onCancel();
  }

  willDestroy(): void {
    super.willDestroy();
    this.dialogElement?.removeEventListener('close', this.onDialogClose);
  }

  <template>
    <dialog
      data-test-confirm-dialog
      class="p-0 rounded-lg shadow-xl backdrop:bg-black/50 max-w-[calc(100vw-2rem)] sm:max-w-sm w-full"
      ...attributes
      {{didInsert this.onInsert}}
    >
      <div class="p-6">
        <p data-test-confirm-message class="text-sm text-gray-700 mb-6">
          {{@message}}
        </p>
        <div class="flex justify-end gap-3">
          <button
            data-test-confirm-cancel
            type="button"
            class="btn-press min-h-[44px] px-5 py-2 text-sm font-medium text-gray-700 bg-gray-100 rounded-md hover:bg-gray-200"
            {{on "click" this.cancel}}
          >
            {{#if this.cancelLabel}}
              {{this.cancelLabel}}
            {{else}}
              {{t "ui.confirm_dialog.cancel"}}
            {{/if}}
          </button>
          <button
            data-test-confirm-ok
            type="button"
            class={{this.confirmButtonClass}}
            {{on "click" this.confirm}}
          >
            {{#if this.confirmLabel}}
              {{this.confirmLabel}}
            {{else}}
              {{t "ui.confirm_dialog.confirm"}}
            {{/if}}
          </button>
        </div>
      </div>
    </dialog>
  </template>
}
