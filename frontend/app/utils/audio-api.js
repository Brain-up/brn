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
  }
}

export default function audioApi() {
  return true;
}

export function toSeconds(value) {
  return value / 1000;
}
export function toMilliseconds(value) {
  return value * 1000;
}

var CrossfadeSample = {playing:false};

export function createSource(context, buffer) {
  var source = context.createBufferSource();
  var gainNode = context.createGain ? context.createGain() : context.createGainNode();
  source.buffer = buffer;
  // Turn off looping
  source.loop = false;
  // Connect source to gain.
  source.connect(gainNode);
  // Connect gain to destination.
  gainNode.connect(context.destination);

  return {
    source: source,
    gainNode: gainNode
  };
}

export function BufferLoader(context, urlList, callback) {
  this.context = context;
  this.urlList = urlList;
  this.onload = callback;
  this.bufferList = new Array();
  this.loadCount = 0;
}

BufferLoader.prototype.loadBuffer = function(url, index) {
  // Load buffer asynchronously
  var request = new XMLHttpRequest();
  request.open("GET", url, true);
  request.responseType = "arraybuffer";

  var loader = this;

  request.onload = function() {
    // Asynchronously decode the audio file data in request.response
    loader.context.decodeAudioData(
      request.response,
      function(buffer) {
        if (!buffer) {
          alert('error decoding file data: ' + url);
          return;
        }
        loader.bufferList[index] = buffer;
        if (++loader.loadCount == loader.urlList.length)
          loader.onload(loader.bufferList);
      },
      function() {
        //
      }
    );
  }

  request.onerror = function() {
    alert('BufferLoader: XHR error');
  }

  request.send();
}

BufferLoader.prototype.load = function() {
  for (var i = 0; i < this.urlList.length; ++i)
  this.loadBuffer(this.urlList[i], i);
}
