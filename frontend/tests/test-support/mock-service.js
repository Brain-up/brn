import Service from '@ember/service';

export default function mockService(owner, name, fields) {
  class MockServiceClass extends Service {
    constructor() {
      super(...arguments);
      Object.assign(this, fields);
    }
  }
  owner.register(`service:${name}`, MockServiceClass);
}
