import BaseTaskSerializer from '../task';
import Model from '@ember-data/model';
import Exercise from 'brn/models/exercise';

export default class TaskSignalSerializer extends BaseTaskSerializer {
  payloadToTypeId(payload: { id: number }) {
    return { id: `signal-task-${payload.id}`, type: 'task/signal' };
  }
  // @ts-expect-error
  normalize(_: Model, hash: any, parent: Exercise) {
    const { id, type } = this.payloadToTypeId(hash);
    const store = this.store;
    const opts = parent.signals.map((el: { id: string }, i: number) => {
      return {
        get word() {
          return `${i + 1}: [${this.signal.duration}ms, ${
            this.signal.frequency
          }Mhz]`;
        },
        get signal() {
          return store.peekRecord('signal', el.id);
        },
        get audioFileUrl() {
          return this.signal;
        },
      };
    });
    const attrs = {
      ...hash,
      exerciseType: 'signal',
      answerOptions: opts,
      normalizedAnswerOptions: opts,
    };
    return {
      id,
      type,
      attributes: attrs,
      relationships: {
        signal: {
          data: {
            id: hash.id,
            type: 'signal',
          },
        },
        exercise: {
          data: {
            id: parent.id,
            type: 'exercise',
          },
        },
      },
    };
  }
}

// DO NOT DELETE: this is how TypeScript knows how to look up your serializers.
declare module 'ember-data/types/registries/serializer' {
  export default interface SerializerRegistry {
    'task/signal': TaskSignalSerializer;
  }
}
