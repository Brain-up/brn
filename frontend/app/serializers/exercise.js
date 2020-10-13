import ApplicationSerializer from './application';

export default class ExerciseSerializer extends ApplicationSerializer {
  ATTR_NAMES_MAP = Object.freeze({
    order: 'level',
  });
  attrs = {
    tasks: { serialize: 'ids-and-types', deserialize: 'records' },
    signals: { serialize: 'ids-and-types', deserialize: 'records' },
  };
  normalizeSignal(store, payloadItem) {
    const included = [];
    const signalSerializer = store.serializerFor('signal');
    const taskSignalSerializer = store.serializerFor('task/signal');
    payloadItem.signals = payloadItem.signals.map((el) => {
      const normalizedSignal = signalSerializer.normalize(store.modelFor('signal'), el);
      included.push(normalizedSignal);
      included.push(taskSignalSerializer.normalize(store.modelFor('task/signal'), normalizedSignal, payloadItem));
      return signalSerializer.payloadToTypeId(el);
    })
    payloadItem.tasks = payloadItem.signals.map((el)=> {
      return taskSignalSerializer.payloadToTypeId(el);
    });
    if (included.length) {
      store.push({ data: included });
    }
  }
  normalizeResponse(store, primaryModelClass, payload, id, requestType) {
    const data = payload?.data;
    if (Array.isArray(data)) {
      data.map((el) => {
        if (el.signals) {
          this.normalizeSignal(store, el);
        }
      });
    }
    return super.normalizeResponse(store, primaryModelClass, payload, id, requestType);
  }
}
