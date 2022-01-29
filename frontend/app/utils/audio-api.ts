import type { FFmpeg } from '@ffmpeg/ffmpeg';
import Ember from 'ember';

let ffmpeg: FFmpeg | null = null;

async function initFFmpeg() {
  if (!globalThis.crossOriginIsolated) {
    return;
  }
  const { createFFmpeg } = await import('@ffmpeg/ffmpeg');
  if (ffmpeg === null) {
    const corePath =
      window.location.protocol +
      '//' +
      window.location.host +
      '/assets/ffmpeg-core.js';
    ffmpeg = createFFmpeg({ log: false, corePath });
  }
  if (!ffmpeg.isLoaded()) {
    try {
      await ffmpeg.load();
    } catch (e) {
      console.error(e);
    }
  }
}

export async function transcodeFile(file: ArrayBuffer) {
  if (!ffmpeg) {
    return file;
  }

  const inputName = `${Math.random().toString(16).slice(2, 8)}.ogg`;
  const outputName = `${Math.random().toString(16).slice(2, 8)}.wav`;

  ffmpeg.FS('writeFile', inputName, new Uint8Array(file));
  await ffmpeg.run('-i', inputName, outputName);
  const data = ffmpeg.FS('readFile', outputName);
  ffmpeg.FS('unlink', inputName);
  ffmpeg.FS('unlink', outputName);

  return await new Blob([data.buffer], { type: 'audio/wav' }).arrayBuffer();
}

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

export function createNoizeBuffer(
  context: BaseAudioContext,
  duration: number,
  level: number,
) {
  const channels = 2;
  const frameCount = context.sampleRate * duration;
  const myArrayBuffer = context.createBuffer(
    channels,
    frameCount,
    context.sampleRate,
  );

  for (let channel = 0; channel < channels; channel++) {
    const nowBuffering = myArrayBuffer.getChannelData(channel);
    for (let i = 0; i < frameCount; i++) {
      nowBuffering[i] = (Math.random() * 2 - 1) * level * 0.01;
    }
  }
  return myArrayBuffer;
}

export default function audioApi() {
  return true;
}

export function toSeconds(value: number) {
  return value / 1000;
}
export function toMilliseconds(value: number) {
  return value * 1000;
}

export function createAudioContext() {
  const AudioContext =
    window.AudioContext || (window as any).webkitAudioContext;
  return new AudioContext();
}

export interface ISource {
  source: AudioBufferSourceNode;
  gainNode: GainNode;
}

export function createSource(
  context: BaseAudioContext,
  buffer: AudioBuffer,
): ISource {
  const source = context.createBufferSource();
  const gainNode: GainNode = context.createGain
    ? context.createGain()
    : (context as any).createGainNode();
  source.buffer = buffer;
  source.loop = false;
  source.connect(gainNode);
  gainNode.connect(context.destination);

  return {
    source: source,
    gainNode: gainNode,
  };
}

export class BufferLoader {
  context!: BaseAudioContext;
  urlList!: string[];
  onload!: (results: (AudioBuffer | null)[]) => void;
  getTokenCallback!: () => string;
  bufferList = [];
  constructor(
    context: BaseAudioContext,
    urlList: string[],
    callback: (results: AudioBuffer[]) => void,
    getTokenCallback: () => string,
  ) {
    this.context = context;
    this.urlList = urlList;
    this.onload = callback;
    this.getTokenCallback = getTokenCallback;
  }
  async load() {
    const files = await Promise.all(
      this.urlList.map((url) =>
        arrayBufferRequest(url, this.getTokenCallback()),
      ),
    );
    try {
      const results: (AudioBuffer | null)[] = [];

      for (const file of files) {
        if (file === null) {
          results.push(null);
        } else {
          let result = null;
          const fileClone = file.slice(0);
          try {
            result = await this.context.decodeAudioData(file);
          } catch (e) {
            try {
              await initFFmpeg();
              result = await this.context.decodeAudioData(
                await transcodeFile(fileClone),
              );
            } catch (e) {
              // EOL
            }
          }
          results.push(result);
        }
      }
      return this.onload(results);
    } catch (e) {
      console.error(e, files, this.urlList);
      return [];
    }
  }
}

const AudioCache = new Map();

function arrayBufferRequest(
  url: string,
  token: string,
): Promise<ArrayBuffer | null> {
  const urlObj = new URL(url);
  return new Promise((resolve) => {
    if (AudioCache.has(url)) {
      return resolve(AudioCache.get(url).slice());
    }
    const request = new XMLHttpRequest();
    request.open('GET', url, true);
    if (urlObj.search !== '') {
      // we don't need it for amazon api, we use it without query params
      request.setRequestHeader('Authorization', `Bearer ${token}`);
    }
    request.responseType = 'arraybuffer';
    request.onload = function () {
      AudioCache.set(url, request.response.slice());
      resolve(request.response);
    };
    request.onerror = function () {
      resolve(null);
    };
    request.send();
  });
}

export function loadAudioFiles(
  context: BaseAudioContext,
  files: string[],
  getToken: () => string,
): Promise<(AudioBuffer | null)[]> {
  return new Promise((resolve) => {
    const bufferLoader = new BufferLoader(
      context,
      [...files],
      resolve,
      getToken,
    );
    bufferLoader.load();
  });
}
