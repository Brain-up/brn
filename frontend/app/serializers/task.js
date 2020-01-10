import ApplicationSerializer from './application';

export default ApplicationSerializer.extend({
  ATTR_NAMES_MAP: Object.freeze({
    order: 'serialNumber',
    type: 'exerciseType',
  }),
});
