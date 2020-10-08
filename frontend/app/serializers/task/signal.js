import BaseTaskSerializer from '../task';

export default class TaskSignalSerializer extends BaseTaskSerializer {
  payloadToTypeId(payload) {
    return { id: `signal-task-${payload.id}`, type: 'task/signal' }
  }
  normalize(typeClass, hash, parent) {
    const { id, type } = this.payloadToTypeId(hash);
    const attrs = {...hash, exerciseType: 'signal'};
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
