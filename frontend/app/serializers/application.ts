import JSONSerializer from '@ember-data/serializer/json';
import Store from '@ember-data/store';
import Model from '@ember-data/model';

type GroupDTO = {
  description: string;
  id: number;
  locale: 'ru-ru' | 'en-us';
  name: string;
  series: number[];
};

type ExerciseDTOType =
  | 'WORDS_SEQUENCES'
  | 'SENTENCE'
  | 'SINGLE_SIMPLE_WORDS'
  | 'PHRASES'
  | 'DI'
  | 'DURATION_SIGNALS'
  | 'FREQUENCY_SIGNALS';

type SeriesDTO = {
  description: string;
  group: number;
  id: number;
  level: number;
  name: string;
  subGroups: number[];
  type: ExerciseDTOType;
};

type SubgroupDTO = {
  description: string;
  exercises: number[];
  id: number;
  level: number;
  name: string;
  pictureUrl: string;
  seriesId: number;
};

type ExerciseDTO = {
  available: boolean;
  id: number;
  level: number;
  name: string;
  noise: { level: number; url: string };
  seriesId: number;
  signals: [];
  tasks: { id: number }[];
  template: string;
};

type AnswerDTO = {
  audioFileUrl: string;
  id: number;
  pictureFileUrl: string;
  soundsCount: number;
  word: string;
  wordType: 'OBJECT';
};

type TaskDTO = {
  answerOptions: AnswerDTO[];
  exerciseType: ExerciseDTOType;
  id: number;
  name: string;
  serialNumber: number;
};

type DTOItem = GroupDTO | SeriesDTO | SubgroupDTO | ExerciseDTO | TaskDTO;

type GenericDTO = {
  data: DTOItem[];
};

export default class ApplicationSerializer extends JSONSerializer {
  ATTR_NAMES_MAP: { [key: string]: string } = Object.freeze({});

  normalizeResponse(
    store: Store,
    primaryModelClass: Model,
    payload: GenericDTO,
    id: string,
    requestType: string,
  ) {
    const data = payload.data;
    return super.normalizeResponse(
      store,
      primaryModelClass,
      data,
      id,
      requestType,
    );
  }

  normalizeSingleResponse(
    store: Store,
    primaryModelClass: Model,
    payload: DTOItem,
    id: string,
    requestType: string,
  ) {
    const data = Array.isArray(payload) ? payload[0] : payload;
    return super.normalizeSingleResponse(
      store,
      primaryModelClass,
      data,
      id,
      requestType,
    );
  }

  keyForAttribute(attrKey: string) {
    return this.ATTR_NAMES_MAP[attrKey] || attrKey;
  }

  keyForRelationship(attrKey: string) {
    return this.ATTR_NAMES_MAP[attrKey] || attrKey;
  }
}

// DO NOT DELETE: this is how TypeScript knows how to look up your serializers.
declare module 'ember-data/types/registries/serializer' {
  export default interface SerializerRegistry {
    application: ApplicationSerializer;
  }
}
