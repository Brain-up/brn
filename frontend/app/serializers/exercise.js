import ApplicationSerializer from './application';

const ATTR_NAMES_VOCABULARY = {
  order: 'level',
};

export default ApplicationSerializer.extend({
  keyForAttribute(attrKey) {
    return ATTR_NAMES_VOCABULARY[attrKey] || attrKey;
  },
});
