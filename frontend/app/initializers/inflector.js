import Inflector from 'ember-inflector';

export function initialize() {
  const inflector = Inflector.inflector;
  inflector.uncountable('single-simple-words');
  inflector.uncountable('words-sequences');
}

export default {
  name: 'custom-inflector-rules',
  initialize,
};
