import { t } from 'ember-intl';

const TELEGRAM_URL = 'https://t.me/BrainUpUsers';

<template>
  <div class="flex min-h-[60vh] items-center justify-center" data-test-server-down>
    <div class="mx-auto max-w-lg rounded-lg bg-white p-8 text-center shadow-lg">
      <div class="mb-4 text-6xl">&#x26A0;</div>
      <h1 class="mb-4 text-2xl font-bold text-gray-800" data-test-server-down-title>
        {{t "server_down.title"}}
      </h1>
      <p class="mb-4 text-gray-600" data-test-server-down-message>
        {{t "server_down.message"}}
      </p>
      <p class="mb-2 text-gray-600">
        {{t "server_down.telegram_prompt"}}
      </p>
      <a
        href={{TELEGRAM_URL}}
        target="_blank"
        rel="noopener noreferrer"
        class="mb-4 inline-block text-lg font-semibold text-blue-600 hover:text-blue-800 hover:underline"
        data-test-server-down-telegram-link
      >
        {{TELEGRAM_URL}}
      </a>
      <p class="mt-4 text-sm text-gray-500" data-test-server-down-fix-promise>
        {{t "server_down.fix_promise"}}
      </p>
    </div>
  </div>
</template>
