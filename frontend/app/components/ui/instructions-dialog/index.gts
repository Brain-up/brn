import Component from '@glimmer/component';
import { action } from '@ember/object';
import { on } from '@ember/modifier';
import didInsert from '@ember/render-modifiers/modifiers/did-insert';
import { t } from 'ember-intl';

interface UiInstructionsDialogSignature {
  Args: {
    titleKey: string;
    onClose: () => void;
  };
  Blocks: {
    default: [];
  };
  Element: HTMLDialogElement;
}

export default class UiInstructionsDialogComponent extends Component<UiInstructionsDialogSignature> {
  dialogElement: HTMLDialogElement | null = null;

  @action
  onInsert(element: HTMLDialogElement) {
    this.dialogElement = element;
    element.showModal();
    element.addEventListener('close', this.onDialogClose);
  }

  @action
  onDialogClose() {
    this.args.onClose();
  }

  @action
  close() {
    // Remove listener first to prevent async double-call from the native
    // 'close' event, then close and notify synchronously.
    this.dialogElement?.removeEventListener('close', this.onDialogClose);
    this.dialogElement?.close();
    this.args.onClose();
  }

  willDestroy(): void {
    super.willDestroy();
    this.dialogElement?.removeEventListener('close', this.onDialogClose);
  }

  <template>
    <dialog
      data-test-instructions-dialog
      aria-modal="true"
      aria-labelledby="instructions-dialog-title"
      class="p-0 rounded-lg shadow-xl backdrop:bg-black/50 w-full max-w-[calc(100vw-1rem)] sm:max-w-2xl"
      ...attributes
      {{didInsert this.onInsert}}
    >
      <div class="flex flex-col max-h-[85vh]">
        <header class="flex items-start justify-between px-6 pt-6 pb-4 border-b border-gray-100">
          <h2
            id="instructions-dialog-title"
            data-test-instructions-dialog-title
            class="text-xl font-semibold text-gray-800"
          >
            {{t @titleKey}}
          </h2>
          <button
            data-test-instructions-dialog-close-x
            type="button"
            aria-label={{t "instructions.close"}}
            class="btn-press shrink-0 ml-4 min-h-[44px] min-w-[44px] text-gray-500 hover:text-gray-800 text-2xl leading-none"
            {{on "click" this.close}}
          >
            ×
          </button>
        </header>
        <div class="px-6 py-5 overflow-y-auto font-openSans text-base leading-relaxed text-gray-700">
          {{yield}}
        </div>
        <footer class="flex justify-end px-6 py-4 border-t border-gray-100">
          <button
            data-test-instructions-dialog-close
            type="button"
            class="btn-press min-h-[44px] px-6 py-2 text-sm font-semibold text-white bg-indigo-600 rounded-md hover:bg-indigo-700"
            {{on "click" this.close}}
          >
            {{t "instructions.close"}}
          </button>
        </footer>
      </div>
    </dialog>
  </template>
}
