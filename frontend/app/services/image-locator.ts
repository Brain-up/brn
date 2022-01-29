import Service, { inject as service } from '@ember/service';
import UserDataService from 'brn/services/user-data';

export default class ImageLocatorService extends Service {
  @service('user-data') userData!: UserDataService;
  async getPictureForWord(word: string): Promise<string | null> {
    const images = await Promise.all([
      this.getImageFromOpensymbols(word),
      this.getImageFromArasaac(word),
    ]);
    return images.find((img) => typeof img === 'string');
  }
  async getImageFromArasaac(word: string) {
    try {
      const lang = this.userData.activeLocale.split('-')[0];
      let symbols2 = await fetch(
        'https://api.arasaac.org/api/pictograms/' +
          lang +
          '/bestsearch/' +
          encodeURIComponent(word),
      );
      let data2 = await symbols2.json();

      if (!data2.length) {
        let fixedWord = word.endsWith('у') ? word.slice(0, -1) + 'а' : word;
        fixedWord = fixedWord.endsWith('л')
          ? fixedWord.slice(0, -1) + 'ть'
          : fixedWord;
        fixedWord = fixedWord.endsWith('ла')
          ? fixedWord.slice(0, -2) + 'ть'
          : fixedWord;
        fixedWord = fixedWord.endsWith('ны')
          ? fixedWord.slice(0, -2) + 'н'
          : fixedWord;
        fixedWord = fixedWord.endsWith('ов')
          ? fixedWord.slice(0, -2)
          : fixedWord;
        fixedWord = fixedWord.endsWith('ли')
          ? fixedWord.slice(0, -2)
          : fixedWord;
        fixedWord = fixedWord.endsWith('ует')
          ? fixedWord.slice(0, -3) + 'вать'
          : fixedWord;
        fixedWord = fixedWord.endsWith('ет')
          ? fixedWord.slice(0, -2) + 'ть'
          : fixedWord;
        fixedWord = fixedWord.endsWith('ит')
          ? fixedWord.slice(0, -2) + 'ать'
          : fixedWord;
        fixedWord = fixedWord.endsWith('ву')
          ? fixedWord.slice(0, -2) + 'ва'
          : fixedWord;
        symbols2 = await fetch(
          'https://api.arasaac.org/api/pictograms/' +
            lang +
            '/search/' +
            encodeURIComponent(fixedWord),
        );
        data2 = await symbols2.json();
      }

      if (!data2.length) {
        symbols2 = await fetch(
          'https://api.arasaac.org/api/pictograms/' +
            lang +
            '/search/' +
            encodeURIComponent(word),
        );
        data2 = await symbols2.json();
      }

      if (data2.length) {
        const id = data2[0]._id;
        return `https://static.arasaac.org/pictograms/${id}/${id}_500.png`;
      }
      return null;
    } catch (e) {
      return null;
    }
  }
  async getImageFromOpensymbols(word: string) {
    try {
      const symbols = await fetch(
        'https://www.opensymbols.org/api/v1/symbols/search?q=' +
          encodeURIComponent(word) +
          '&locale=' +
          encodeURIComponent(this.userData.activeLocale.split('-')[0]),
      );
      const data = await symbols.json();
      if (data.length) {
        return data[0].image_url;
      }
      return null;
    } catch (e) {
      return null;
    }
  }
}

// DO NOT DELETE: this is how TypeScript knows how to look up your services.
declare module '@ember/service' {
  // eslint-disable-next-line no-unused-vars
  interface Registry {
    'image-locator': ImageLocatorService;
  }
}
