<template>
  <div ...attributes>
    <button
      data-test-start-task-button
      aria-label={{t "start_task_button.label"}}
      type="button"
      class="btn-press"
      {{on "click" @startTask}}
    >
      <FaIcon class="play-button-icon" @icon="play" />
    </button>
  </div>
</template>
