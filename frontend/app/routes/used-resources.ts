import Route from '@ember/routing/route';

export default class UsedResourcesRoute extends Route {
  model() {
    return {
      resources: [
        {
          title: 'freepik.com',
          href: 'https://freepik.com',
        },
        {
          title: "Plants (Köhler's Medizinal-Pflanzen)",
          href: 'https://ru.wikipedia.org/wiki/%D0%A1%D0%BF%D0%B8%D1%81%D0%BE%D0%BA_%D1%80%D0%B0%D1%81%D1%82%D0%B5%D0%BD%D0%B8%D0%B9,_%D0%B8%D0%BB%D0%BB%D1%8E%D1%81%D1%82%D1%80%D0%B0%D1%86%D0%B8%D0%B8_%D0%BA_%D0%BA%D0%BE%D1%82%D0%BE%D1%80%D1%8B%D0%BC_%D1%80%D0%B0%D0%B7%D0%BC%D0%B5%D1%89%D0%B5%D0%BD%D1%8B_%D0%B2_%D1%81%D0%BF%D1%80%D0%B0%D0%B2%D0%BE%D1%87%D0%BD%D0%B8%D0%BA%D0%B5_K%C3%B6hler%E2%80%99s_Medizinal-Pflanzen',
        },
        {
          title:
            'Flora («Flora von Deutschland, Österreich und der Schweiz in Wort und Bild für Schule und Haus»)',
          href: 'https://web.archive.org/web/20090601012031/http://caliban.mpiz-koeln.mpg.de/~stueber/thome/Alphabetical_list.html',
        },
      ],
    };
  }
}
