import Inflector from 'ember-inflector';

export function initialize(): void {
  const inflector = Inflector.inflector;
  inflector.uncountable('single-simple-words');
  inflector.uncountable('words-sequences');
}

export default {
  name: 'custom-inflector-rules',
  initialize,
} as { name: string; initialize: () => void };
