import Route from '@ember/routing/route';
import NetworkService from 'brn/services/network';
import { inject as service } from '@ember/service';
import Intl from 'ember-intl/services/intl';

interface IContributorDTO {
    "id": number;
    "name": string;
    "nameEn": string;
    "description": string;
    "descriptionEn": string;
    "company": null;
    "companyEn": null;
    "pictureUrl": string;
    "contacts": [];
    "type": "SPECIALIST" | "DEVELOPER";
    "contribution": number;
    "active": boolean;
}

export default class SpecialistsRoute extends Route {
    @service('network') network!: NetworkService;
    @service('intl') intl!: Intl;
    async model() {
        const request = await this.network.request('contributors');
        const { data } : { data: IContributorDTO[] } = await request.json();
        const specialists = data.filter(e => e.type === 'SPECIALIST' && e.active);
        const lang = this.intl.locale[0];

        return specialists.map(e => ({
            id: e.id,
            name: e[`name${lang === 'ru-ru' ? '' : 'En'}`],
            description: e[`description${lang === 'ru-ru' ? '' : 'En'}`],
            company: e[`company${lang === 'ru-ru' ? '' : 'En'}`],
            avatar: e.pictureUrl,
            contribution: e.contribution
        }));
    }
}