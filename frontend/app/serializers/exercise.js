import ApplicationSerializer from './application';

export default ApplicationSerializer.extend({
  ATTR_NAMES_MAP: Object.freeze({
    order: 'level',
  }),
  attrs: {
    tasks: { serialize: 'ids-and-types', deserialize: 'records' },
  },
});
