import Ember from 'ember';

export const TIMINGS = {
  _step: 100,

  FAKE_AUDIO: 200,
  get FAKE_AUDIO_STARTED() {
    return this.FAKE_AUDIO - this._step;
  },
  get FAKE_AUDIO_FINISHED() {
    return this._step * 2;
  },

  get SUCCESS_ANSWER_NOTIFICATION() {
    return Ember.testing ? 200 : 3000;
  },
  get SUCCESS_ANSWER_NOTIFICATION_STARTED() {
    return this.SUCCESS_ANSWER_NOTIFICATION - this._step;
  },
  get SUCCESS_ANSWER_NOTIFICATION_FINISHED() {
    return this.SUCCESS_ANSWER_NOTIFICATION + this._step;
  },
};

export default function audioApi() {
  return true;
}

export function toSeconds(value) {
  return value / 1000;
}
export function toMilliseconds(value) {
  return value * 1000;
}

export function createAudioContext() {
  const AudioContext = window.AudioContext || window.webkitAudioContext;
  return new AudioContext();
}

export function createSource(context, buffer) {
  const source = context.createBufferSource();
  const gainNode = context.createGain
    ? context.createGain()
    : context.createGainNode();
  source.buffer = buffer;
  source.loop = false;
  source.connect(gainNode);
  gainNode.connect(context.destination);

  return {
    source: source,
    gainNode: gainNode,
  };
}

export function BufferLoader(context, urlList, callback) {
  this.context = context;
  this.urlList = urlList;
  this.onload = callback;
  this.bufferList = new Array();
}

function arrayBufferRequest(url) {
  return new Promise((resolve, reject) => {
    const request = new XMLHttpRequest();
    request.open('GET', url, true);
    request.responseType = 'arraybuffer';
    request.onload = function() {
      resolve(request.response);
    };
    request.onerror = function() {
      reject(new Error('BufferLoader: XHR error'));
    };
    request.send();
  });
}

BufferLoader.prototype.load = async function() {
  const files = await Promise.all(
    this.urlList.map((url) => arrayBufferRequest(url)),
  );
  const results = await Promise.all(
    files.map((file) => {
      return new Promise((resolve, reject) => {
        this.context.decodeAudioData(file, resolve, reject);
      });
    }),
  );
  return this.onload(results);
};
