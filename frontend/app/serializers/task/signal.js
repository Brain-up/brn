import BaseTaskSerializer from '../task';

export default class TaskSignalSerializer extends BaseTaskSerializer {
  payloadToTypeId(payload) {
    return { id: `signal-task-${payload.id}`, type: 'task/signal' }
  }
  normalize(typeClass, hash, parent) {
    const { id, type } = this.payloadToTypeId(hash);
    const store = this.store;
    const opts = parent.signals.map((el, i)=>{
      return {
        get word() {
          return `${i+1}: [${this.signal.duration}ms, ${this.signal.frequency}Mhz]`;
        },
        get signal() {
          return store.peekRecord('signal', el.id);
        },
        get audioFileUrl() {
          return this.signal;
        }
      }
    })
    const attrs = {
      ...hash, exerciseType: 'signal',
      answerOptions: opts,
      normalizedAnswerOptions: opts
    };
    return {
      id,
      type,
      attributes: attrs,
      relationships: {
        signal: {
          data: {
            id: hash.id,
            type: 'signal'
          }
        },
        exercise: {
          data: {
            id: parent.id,
            type: 'exercise'
          }
        }
      }
    }
  }
}
