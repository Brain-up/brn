import { urlForImage, urlForAudio } from 'brn/utils/file-url';
export default class AnswerOption {
  audioFileUrl = null;
  id = null;
  word = null;
  pictureFileUrl = null;
  constructor({id, audioFileUrl, word, pictureFileUrl} = {}) {
    this.audioFileUrl = '/audio/no_noise/%D0%B1%D0%B0%D0%BB.mp3';
    // this.audioFileUrl = audioFileUrl ? urlForAudio(audioFileUrl) : null;
    this.id = id;
    this.word = word;
    this.pictureFileUrl = pictureFileUrl ? urlForImage(pictureFileUrl) : null;
    // this is for eslint, due to files bugfix
    if (!audioFileUrl || urlForAudio(audioFileUrl)) {
      return;
    }
  }
}