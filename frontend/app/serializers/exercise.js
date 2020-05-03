import ApplicationSerializer from './application';

export default class ExerciseSerializer extends ApplicationSerializer {
  ATTR_NAMES_MAP = Object.freeze({
    order: 'level',
  });
  attrs = {
    tasks: { serialize: 'ids-and-types', deserialize: 'records' },
  };
}
