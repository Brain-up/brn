import Model from '@ember-data/model';
import ApplicationSerializer, { IContributorDTO } from './application';

export default class ContributorSerializer extends ApplicationSerializer {
  normalize(typeClass: Model, rawPayload: IContributorDTO) {
    const payload = {
      id: rawPayload.id.toString(),
      rawName: {
        'ru-ru': rawPayload.name,
        'en-us': rawPayload.nameEn,
      },
      rawDescription: {
        'ru-ru': rawPayload.description,
        'en-us': rawPayload.descriptionEn,
      },
      rawCompany: {
        'ru-ru': rawPayload.company,
        'en-us': rawPayload.companyEn,
      },
      avatar: rawPayload.pictureUrl,
      contribution: rawPayload.contribution,
      isActive: rawPayload.active,
      kind: rawPayload.type,
      contacts: rawPayload.contacts,
    };
    return super.normalize(typeClass, payload);
  }
}
