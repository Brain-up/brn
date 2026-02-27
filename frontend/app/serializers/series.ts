import ApplicationSerializer from './application';
import Model from '@warp-drive-mirror/legacy/model';

export default class SeriesSerializer extends ApplicationSerializer {
  ATTR_NAMES_MAP = Object.freeze({});
  normalize(typeClass: Model, rawPayload: any) {
    rawPayload.kind = rawPayload.type;
    return super.normalize(typeClass, rawPayload);
  }
}

