import Service, { inject as service } from '@ember/service';
import IntlService from 'ember-intl/services/intl';

interface IDoctorInfo {
    [key: string]: {
        name: String,
        bio: String
    },
}
interface IIntlString {
    ru: String,
    en: String
}

class Doctor {
    img: String;
    lang: IDoctorInfo;

    constructor(img: string, name: IIntlString, bio: IIntlString) {
        this.img = img;
        this.lang = {
            'ru-ru': { name: name.ru, bio: bio.ru },
            'en-us': { name: name.en, bio: bio.en }
        };
    }
}

class TeamMember {
    img: String;

    constructor(img: string) {
        this.img = img;
    }
}

export default class PersonsService extends Service {
    @service('intl') intl!: IntlService;

    get persons() {
        return {
            doctors: this.doctors,
            teamMembers: this.personsData.teamMembers
        }
    }

    get doctors() {
        let locale = this.intl.locale[0];

        return this.personsData.doctors.map((doctor) => {
            let doctorInfo = doctor.lang[locale];
            return {
                img: doctor.img,
                name: doctorInfo.name,
                bio: doctorInfo.bio
            }
        });
    }

    personsData = {
        doctors: [
            new Doctor(
                '/content/pages/description/doctors/inna-koroleva.jpg',
                {
                    ru: 'Королева Инна Васильевна',
                    en: 'Inna Koroleva',
                },
                {
                    ru: 'Научный руководитель реабилитационного отделения, доктор психологических наук, профессор, автор серии методических пособий "Учусь слушать и говорить"',
                    en: 'Academic supervisor of the rehabilitation department, Doctor of Psychology, Professor, author of the manuals "Learning to listen and speak"',
                }
            ), new Doctor(
                '/content/pages/description/doctors/ekaterina-garbaruk.jpg',
                {
                    ru: 'Гарбарук Екатерина Сергеевна',
                    en: 'Ekaterina Garbaruk',
                },
                {
                    ru: 'Кандидат биологических наук, специалист Лаборатории слуха и речи ПСПбГМУ, специалист в области диагностики слуховых нарушений',
                    en: 'Candidate of Biological Sciences, expert at the Laboratory of Hearing and Speech (The Pavlov First Saint Petersburg State Medical University), expert in diagnosis of aural disorders',
                }
            ), new Doctor(
                '/content/pages/description/doctors/lubov-proshina.jpg',
                {
                    ru: 'Прошина Любовь Александровна',
                    en: 'Lubov Proshina',
                },
                {
                    ru: 'Сурдопедагог, РНПЦ оториноларингологии, опыт работы более 10 лет',
                    en: 'Teacher of the deaf, Belarusian Republican Scientific and Practical Center of Otorhinolaryngology, 10+ years of experience',
                }

            ), new Doctor(
                '/content/pages/description/doctors/natalia-metelskaya.jpg',
                {
                    ru: 'Метельская Наталья Николаевна',
                    en: 'Natalia Metelskaya',
                },
                {
                    ru: 'Сурдопедагог, УЗ Могилевская областная детская больница, опыт работы более 20 лет',
                    en: 'Teacher of the deaf, Mogilev Regional Children\'s Hospital (Belarus), 20+ years of experience',
                }
            ), new Doctor(
                '/content/pages/description/doctors/ksenia-berezkina.jpg',
                {
                    ru: 'Березкина Ксения Сергеевна',
                    en: 'Ksenia Berezkina',
                },
                {
                    ru: 'Сурдопедагог, Городской ресурсный центр для детей с нарушением слуха, опыт работы более 10 лет',
                    en: 'Teacher of the deaf, City Resource Center for Hearing Impaired Children, 10+ years of experience',
                }
            ), new Doctor(
                '/content/pages/description/doctors/kristina-sivenkova.jpg',
                {
                    ru: 'Сивенкова Кристина Александровна',
                    en: 'Kristina Sivenkova',
                },
                {
                    ru: 'Сурдопедагог, СПб Сурдоцентр, молодой специалист',
                    en: 'Teacher of the deaf, Saint Petersburg Center of Otorhinolaryngology, young professional',
                }
            ), new Doctor(
                '/content/pages/description/doctors/daria-platonenko.jpg',
                {
                    ru: 'Платоненко Дарья Сергеевна',
                    en: 'Daria Platonenko',
                },
                {
                    ru: 'Сурдопедагог, РНПЦ оториноларингологии, опыт работы более 3 лет',
                    en: 'Teacher of the deaf, Belarusian Republican Scientific and Practical Center of Otorhinolaryngology, 3+ years of experience',
                }
            ), new Doctor(
                '/content/pages/description/doctors/olga-sukhova.jpg',
                {
                    ru: 'Сухова Ольга',
                    en: 'Olga Sukhova',
                },
                {
                    ru: 'Сурдопедагог, дефектолог, автор пособий "Слушать интересно"',
                    en: 'Teacher of the deaf, defectologist, author of the manuals "It is interesting to listen"',
                }
            ), new Doctor(
                '/content/pages/description/doctors/yulia-kibalova.jpg',
                {
                    ru: 'Юлия Кибалова',
                    en: 'Yulia Kibalova',
                },
                {
                    ru: 'Сурдопедагог, дефектолог, Лаборатория слуха и речи ПСПбГМУ, опыт работы более 10 лет',
                    en: 'Teacher of the deaf, defectologist, expert at the Laboratory of Hearing and Speech (The Pavlov First Saint Petersburg State Medical University), 10+ years of experience'
                }
            )
        ],
        teamMembers: [
            new TeamMember('/content/pages/description/team-members/team-member-1.jpg'),
            new TeamMember('/content/pages/description/team-members/team-member-2.jpg'),
            new TeamMember('/content/pages/description/team-members/team-member-3.jpg'),
            new TeamMember('/content/pages/description/team-members/team-member-4.jpg'),
            new TeamMember('/content/pages/description/team-members/team-member-5.jpg'),
            new TeamMember('/content/pages/description/team-members/team-member-6.jpg'),
            new TeamMember('/content/pages/description/team-members/team-member-7.jpg'),
            new TeamMember('/content/pages/description/team-members/team-member-8.jpg'),
            new TeamMember('/content/pages/description/team-members/team-member-9.jpg'),
            new TeamMember('/content/pages/description/team-members/team-member-10.jpg'),
            new TeamMember('/content/pages/description/team-members/team-member-11.jpg'),
            new TeamMember('/content/pages/description/team-members/team-member-12.jpg'),
            new TeamMember('/content/pages/description/team-members/team-member-13.jpg'),
            new TeamMember('/content/pages/description/team-members/team-member-14.jpg'),
            new TeamMember('/content/pages/description/team-members/team-member-15.jpg'),
            new TeamMember('/content/pages/description/team-members/team-member-16.jpg'),
            new TeamMember('/content/pages/description/team-members/team-member-17.jpg'),
            new TeamMember('/content/pages/description/team-members/team-member-18.jpg'),
            new TeamMember('/content/pages/description/team-members/team-member-19.jpg'),
            new TeamMember('/content/pages/description/team-members/team-member-20.jpg'),
            new TeamMember('/content/pages/description/team-members/team-member-21.jpg'),
            new TeamMember('/content/pages/description/team-members/team-member-22.jpg'),
            new TeamMember('/content/pages/description/team-members/team-member-23.jpg'),
            new TeamMember('/content/pages/description/team-members/team-member-24.jpg'),
            new TeamMember('/content/pages/description/team-members/team-member-25.jpg'),
            new TeamMember('/content/pages/description/team-members/team-member-26.jpg'),
            new TeamMember('/content/pages/description/team-members/team-member-27.jpg'),
            new TeamMember('/content/pages/description/team-members/team-member-28.jpg'),
            new TeamMember('/content/pages/description/team-members/team-member-29.jpg'),
            new TeamMember('/content/pages/description/team-members/team-member-30.jpg'),
            new TeamMember('/content/pages/description/team-members/team-member-31.jpg'),
            new TeamMember('/content/pages/description/team-members/team-member-32.jpg'),
            new TeamMember('/content/pages/description/team-members/team-member-33.jpg'),
            new TeamMember('/content/pages/description/team-members/team-member-34.jpg'),
            new TeamMember('/content/pages/description/team-members/team-member-35.jpg'),
            new TeamMember('/content/pages/description/team-members/team-member-36.jpg'),
            new TeamMember('/content/pages/description/team-members/team-member-37.jpg'),
        ]
    }
};

// DO NOT DELETE: this is how TypeScript knows how to look up your models.
declare module '@ember/service' {
    interface Registry {
        'persons': PersonsService;
    }
}