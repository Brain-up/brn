import UiTabButton from 'brn/components/ui/tab-button';

<template>
  <div class="flex flex-1">
    <UiTabButton ...attributes @mode={{@mode}}>{{yield}}</UiTabButton>
  </div>
</template>
