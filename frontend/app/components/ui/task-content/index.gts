import htmlSafe from 'brn/helpers/html-safe';

<template>
  <div style={{htmlSafe "flex: 1 0 auto;"}}>
    {{yield}}
  </div>
</template>
