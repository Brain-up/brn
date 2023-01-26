import ApplicationSerializer from './application';
import Store from '@ember-data/store';
export default class ExerciseSerializer extends ApplicationSerializer {
  ATTR_NAMES_MAP = Object.freeze({
    order: 'level',
  });
  attrs = {
    tasks: { serialize: 'ids-and-types', deserialize: 'records' },
    signals: { serialize: 'ids-and-types', deserialize: 'records' },
  };
  modelNameFromPayloadKey(key: string) {
    return super.modelNameFromPayloadKey(key);
  }
  normalizeSignal(store: Store, payloadItem: any) {
    const included: any[] = [];
    const signalSerializer = store.serializerFor('signal');
    const taskSignalSerializer = store.serializerFor('task/signal');
    payloadItem.signals = payloadItem.signals.map((el) => {
      const normalizedSignal = signalSerializer.normalize(
        store.modelFor('signal'),
        el,
      );
      included.push(normalizedSignal);
      included.push(
        taskSignalSerializer.normalize(
          store.modelFor('task/signal'),
          normalizedSignal,
          payloadItem,
        ),
      );
      return signalSerializer.payloadToTypeId(el);
    });
    payloadItem.tasks = payloadItem.signals.map((el) => {
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

    const modelForMechanism = {
      'MATRIX': 'task/words-sequences',
      'SIGNALS': 'task/signal',
      'WORDS': 'task/single-simple-words',
    }
    items.forEach((el) => {
      el.tasks.forEach((task: { exerciseMechanism?: string }) => {
        if ('exerciseMechanism' in task) {
          task.type = modelForMechanism[task.exerciseMechanism];
        } else {
          task.type = 'task/signal';
        }
      });
    });

    return super.normalizeResponse(
      store,
      primaryModelClass,
      payload,
      id,
      requestType,
    );
  }
}

// DO NOT DELETE: this is how TypeScript knows how to look up your serializers.
declare module 'ember-data/types/registries/serializer' {
  export default interface SerializerRegistry {
    exercise: ExerciseSerializer;
  }
}
