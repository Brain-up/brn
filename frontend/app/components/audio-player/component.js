import Ember from 'ember';
import Component from '@ember/component';
import { isArray } from '@ember/array';
import { action } from '@ember/object';
import { inject as service } from '@ember/service';
import customTimeout from '../../utils/custom-timeout';
import { timeout, task } from 'ember-concurrency';
import { next } from '@ember/runloop';
import { tracked } from '@glimmer/tracking';

var CrossfadeSample = {playing:false};

function createSource(context, buffer) {
  var source = context.createBufferSource();
  var gainNode = context.createGain ? context.createGain() : context.createGainNode();
  source.buffer = buffer;
  // Turn on looping
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

CrossfadeSample.play = function() {
  // Create two sources.
  this.ctl1 = createSource(BUFFERS.drums);
  this.ctl2 = createSource(BUFFERS.organ);
  // Mute the second source.
  this.ctl1.gainNode.gain.value = 0;
  // Start playback in a loop
  if (!this.ctl1.source.start) {
    this.ctl1.source.noteOn(0);
    this.ctl2.source.noteOn(0);
  } else {
    this.ctl1.source.start(0);
    this.ctl2.source.start(0);
  }

};

CrossfadeSample.stop = function() {
  if (!this.ctl1.source.stop) {
    this.ctl1.source.noteOff(0);
    this.ctl2.source.noteOff(0);
  } else {
    this.ctl1.source.stop(0);
    this.ctl2.source.stop(0);
  }
};

// Fades between 0 (all source 1) and 1 (all source 2)
CrossfadeSample.crossfade = function(element) {
  var x = parseInt(element.value) / parseInt(element.max);
  // Use an equal-power crossfading curve:
  var gain1 = Math.cos(x * 0.5*Math.PI);
  var gain2 = Math.cos((1.0 - x) * 0.5*Math.PI);
  this.ctl1.gainNode.gain.value = gain1;
  this.ctl2.gainNode.gain.value = gain2;
};

CrossfadeSample.toggle = function() {
  this.playing ? this.stop() : this.play();
  this.playing = !this.playing;
};

function BufferLoader(context, urlList, callback) {
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
      function(error) {
        console.error('decodeAudioData error', error);
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

export default class AudioPlayerComponent extends Component {
  tagName = '';
  init() {
    super.init(...arguments);
    const AudioContext = window.AudioContext||window.webkitAudioContext;
    this.context = new AudioContext();
    next(() => {
      this.audio.register(this);
    });
  }

  @service audio;

  @(task(function*(){
    this.startTime = Date.now();
    this.setProgress(0);
    while (this.isPlaying) {
      this.setProgress((100 / this.totalDuration) * (Date.now() - this.startTime));
      yield timeout(100);
    }
    yield timeout(100);
    this.setProgress(0);
  }).enqueue()) trackProgress;


  @tracked autoplay = false;

  @tracked isPlaying = false;


  @tracked audioPlayingProgress = 0;

  @tracked previousPlayedUrls;

  @tracked audioFileUrl;

  async didReceiveAttrs() {
    await this.setAudioElements();
    if (this.autoplay && this.previousPlayedUrls !== this.audioFileUrl) {
      await this.playAudio();
    }
  }

  willDestroyElement() {
    this.animationInterval && clearInterval(this.animationInterval);
  }
  get filesToPlay() {
    return isArray(this.audioFileUrl) ? this.audioFileUrl : [this.audioFileUrl];
  }

  async setAudioElements() {
    await new Promise((resolve)=>setTimeout(resolve, 1000));
    const buffers = await new Promise((resolve)=>{
      let bufferLoader = new BufferLoader(
        this.context,
        [
          ...this.filesToPlay
        ],
        resolve
      );
      bufferLoader.load();
    });
    this.sources = buffers.map((buffer)=>createSource(this.context, buffer));
  }


  @action
  async playAudio() {
    this.totalDuration = this.sources.reduce((result, item)=>{
      return result + item.source.buffer.duration * 1000;
    }, 0);
    this.isPlaying = true;
    this.trackProgress.perform();
    for (const item of this.sources) {
      const duration = item.source.buffer.duration * 1000;
      item.source.start(0);
      await new Promise((resolve)=>setTimeout(resolve, duration));
    }
    this.isPlaying = false;

    // /* eslint-disable no-unused-vars */
    // for (let audioElement of this.audioElements) {
    //   if (!this.isDestroyed && !this.isDestroying) {
    //     if (Ember.testing) {
    //       this.isPlaying = true;
    //     } else {
    //       audioElement.play();
    //     }

    //     await customTimeout(audioElement.duration * 1000);
    //   }
    // }

    !this.isDestroyed && !this.isDestroying
      ? this.set('previousPlayedUrls', this.audioFileUrl)
      : '';
    if (Ember.testing) {
      this.isPlaying = false;
    }
  }

  setProgress(progress) {
    window.requestAnimationFrame(() => {
      if (this.buttonElement) {
        this.buttonElement.style.setProperty('--progress', `${progress}%`);
      }
    });
    this.set('audioPlayingProgress', progress);

    if (progress === 100) {
    } else if (progress >= 99 || Ember.testing) {
      this.setProgress(100);
    }
  }
}
