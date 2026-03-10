import { t } from 'ember-intl';

<template>
  <section data-test-doctor-feedback class="sm:p-8 lg:p-16 p-4 bg-white">
    <h2 class="sm:text-4xl sm:mb-6 mb-4 text-2xl font-semibold text-center text-gray-700">
      {{t "doctor_feedback.title"}}
    </h2>
    <div class="max-w-screen-lg m-auto">
      <div class="sm:p-8 lg:p-12 p-4 bg-gradient-to-r from-blue-50 to-purple-50 rounded-lg">
        <div class="font-openSans text-lg leading-relaxed text-gray-600 italic">
          {{t "doctor_feedback.text"}}
        </div>
        <div class="mt-6 text-right">
          <p class="font-semibold text-gray-700">
            {{t "doctor_feedback.name"}}
          </p>
          <p class="font-openSans text-sm text-gray-500">
            {{t "doctor_feedback.credentials"}}
          </p>
        </div>
      </div>
    </div>
  </section>
</template>
