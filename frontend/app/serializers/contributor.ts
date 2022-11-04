import Model from '@ember-data/model';
import ApplicationSerializer, { IContributorDTO } from './application';

export default class ContributorSerializer extends ApplicationSerializer {
  normalize(typeClass: Model, rawPayload: IContributorDTO) {
    const {
      id,
      name,
      nameEn,
      description,
      descriptionEn,
      company,
      companyEn,
      pictureUrl,
      contribution,
      active,
      type,
      contacts,
    } = rawPayload;
    const payload = {
      id: id.toString(),
      rawName: {
        'ru-ru': name ?? '',
        'en-us': nameEn ?? name ?? '',
      },
      rawDescription: {
        'ru-ru': description ?? '',
        'en-us': descriptionEn ?? description ?? '',
      },
      rawCompany: {
        'ru-ru': company ?? '',
        'en-us': companyEn ?? company ?? '',
      },
      avatar: pictureUrl,
      contribution: contribution,
      isActive: active,
      kind: type,
      contacts: contacts,
    };
    return super.normalize(typeClass, payload);
  }
}
