import ApplicationSerializer from './application';
import { getOwner } from '@ember/application';
export default class ExerciseSerializer extends ApplicationSerializer {
  ATTR_NAMES_MAP = Object.freeze({
    order: 'level',
  });
  attrs = {
    tasks: { serialize: 'ids-and-types', deserialize: 'records' },
    signals: { serialize: 'ids-and-types', deserialize: 'records' },
  };
  modelNameFromPayloadKey(key) {
    if (key === 'task/PHRASES') {
      return 'task/phrase';
    }
    if (key === 'task/DURATION_SIGNALS') {
      return 'task/signal';
    }
    if (key === 'task/FREQUENCY_SIGNALS') {
      return 'task/signal';
    }
    return super.modelNameFromPayloadKey(key);
  }
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
        if (el.signals && el.signals.length) {
          this.normalizeSignal(store, el);
        }
      });
    }
    const items = Array.isArray(data) ? data : [data];
    const appRouter = getOwner(this).lookup('route:application');
    const model = appRouter.modelFor('group.series');
    const seriaId = model.toArray().firstObject.seriesId;
    const seria = this.store.peekRecord('series', seriaId);
    const kind = seria.kind;
    items.forEach((el) => {
      el.tasks.forEach((task)=> {
        task.type = `task/${kind}`;
      })
    });
    return super.normalizeResponse(store, primaryModelClass, payload, id, requestType);
  }
}
