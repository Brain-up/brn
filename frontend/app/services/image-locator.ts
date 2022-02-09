import Service, { inject as service } from '@ember/service';
import UserDataService from 'brn/services/user-data';

export default class ImageLocatorService extends Service {
  cache: Map<string, string> = new Map();
  @service('user-data') userData!: UserDataService;
  async getPictureForWordAsDataURL(word: string): Promise<string|null> {
    const url = await this.getPictureForWord(word);
    if (!url) {
      return null;
    }
    return new Promise((resolve, reject) => {
      const img = new Image();
      img.crossOrigin = 'anonymous';
      img.src = url;
      img.onload = () => {
        const canvas = document.createElement('canvas');
        canvas.width = img.width;
        canvas.height = img.height;
        const ctx = canvas.getContext('2d');
        ctx?.drawImage(img, 0, 0);
        const dataURL = canvas.toDataURL('image/png');
        resolve(dataURL);
      };
      img.onerror = () => reject(null);
    });
  }
  async getPictureForWord(word: string): Promise<string | null> {
    if (!this.cache.has(word)) {
      const result = await this._getPictureForWord(word);
      if (result) {
        this.cache.set(word, result);
      }
      return result;
    } else {
      return this.cache.get(word) as string;
    }
  }
  private async _getPictureForWord(word: string): Promise<string | null> {
    const images = await Promise.all([
      this.getImageFromOpensymbols(word),
      this.getImageFromArasaac(word),
    ]);
    if (this.isDestroying) {
      return null;
    }
    const image = images.find((img) => typeof img === 'string');
    if (!image) {
      let trWords = await this.translateWord(word);
      if (this.isDestroying) {
        return null;
      }
      if (!trWords.length) {
        trWords = await this.translateWord(this.fixWord(word));
        if (this.isDestroying) {
          return null;
        }
      }
      for (const trWord of trWords) {
        const result =  await this.getImageFromArasaac(trWord, 'en');
        if (this.isDestroying) {
          return null;
        }
        if (result) {
          return result;
        }
      }
      for (const trWord of trWords) {
        const result =  await this.getImageFromOpensymbols(trWord, 'en');
        if (this.isDestroying) {
          return null;
        }
        if (result) {
          return result;
        }
      }
      return null;
    } else {
      return image;
    }

  }
  async translateWord(word: string, from = 'ru', to = 'en'): Promise<string[]> {
    try {
      // https://yandex.ru/dev/dictionary/doc/dg/reference/lookup.html
      const key = 'dict.1.1.20220129T132020Z.f45a308545a3656b.25a8dceae9820a28d7c8a0d14d464c0bb47953b5';
      const lang = encodeURIComponent(`${from}-${to}`);
      const request = await fetch(`https://dictionary.yandex.net/api/v1/dicservice.json/lookup?key=${key}&lang=${lang}&text=${encodeURIComponent(word)}&flags=4`);
      if (this.isDestroying) {
        return [];
      }
      const data = await request.json();
      return data.def[0].tr.map((e: any) => e.text);
    } catch (e) {
      return [];
    }
  }
  fixWord(word: string) {
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
    fixedWord = fixedWord.endsWith('ок')
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
    return fixedWord;
  }
  private async getImageFromArasaac(word: string, lang = this.userData.activeLocaleShort) {
    try {
      let symbols2 = await fetch(
        'https://api.arasaac.org/api/pictograms/' +
          lang +
          '/bestsearch/' +
          encodeURIComponent(word),
      );
      if (this.isDestroying) {
        return null;
      }
      let data2 = await symbols2.json();
      if (this.isDestroying) {
        return null;
      }
      if (!data2.length) {
        const fixedWord = this.fixWord(word);
        symbols2 = await fetch(
          'https://api.arasaac.org/api/pictograms/' +
            lang +
            '/search/' +
            encodeURIComponent(fixedWord),
        );
        if (this.isDestroying) {
          return null;
        }
        data2 = await symbols2.json();
      }

      if (!data2.length) {
        symbols2 = await fetch(
          'https://api.arasaac.org/api/pictograms/' +
            lang +
            '/search/' +
            encodeURIComponent(word),
        );
        if (this.isDestroying) {
          return null;
        }
        data2 = await symbols2.json();
        if (this.isDestroying) {
          return null;
        }
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
  private async getImageFromOpensymbols(word: string, lang = this.userData.activeLocaleShort) {
    try {
      const symbols = await fetch(
        'https://www.opensymbols.org/api/v1/symbols/search?q=' +
          encodeURIComponent(word) +
          '&locale=' +
          encodeURIComponent(lang),
      );
      if (this.isDestroying) {
        return null;
      }
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
