import ApplicationSerializer from './application';

export default class ExerciseSerializer extends ApplicationSerializer {
  ATTR_NAMES_MAP = Object.freeze({
    order: 'level',
  });
  attrs = {
    tasks: { serialize: 'ids-and-types', deserialize: 'records' },
    signals: { serialize: 'ids-and-types', deserialize: 'records' },
  };
  normalizeResponse(store, primaryModelClass, payload, id, requestType) {
    const included = [];
    payload?.data?.map((el) => {
      if (el.signals) {
        el.signals = el.signals.map((el) => {
          el.duration = el.length;
          included.push({
            id: el.id,
            type: 'signal',
            attributes: { ...el }
          });
          return {
            id: el.id,
            type: 'signal',
          }
        })
      }
    });
    if (included.length) {
      store.push({ data: included });
    }
    return super.normalizeResponse(store, primaryModelClass, payload, id, requestType);
  }
}
