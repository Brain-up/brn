import queryNode from 'brn/helpers/query-node';

<template>
  {{#let (queryNode @selector) as |node|}}
    {{#if node}}
      {{#in-element node}}
        {{yield}}
      {{/in-element}}
    {{/if}}
  {{/let}}
</template>
