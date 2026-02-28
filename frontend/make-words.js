/* eslint-disable @typescript-eslint/no-var-requires */
/* global require */
const fs = require('fs');
const https = require('https');

const words = `бам,сам,дам,зал,бум`;
const token = '';
const folderId = '';

// pip install ffmpeg-normalize
// install ffmpeg

// yc iam create-token
// https://cloud.yandex.ru/docs/speechkit/tts/v3/api-ref/grpc/

const yandex_tts_url = '/tts/v3/utteranceSynthesis';

function YandexTTS(options, callback) {
  const body = JSON.stringify({
    text: options['text'],
    outputAudioSpec: {
      containerAudio: {
        containerAudioType: 'OGG_OPUS',
      },
    },
    hints: [
      { voice: 'filipp' },
      { role: 'friendly' },
    ],
    loudnessNormalizationType: 'LUFS',
  });

  const reqOptions = {
    hostname: 'tts.api.cloud.yandex.net',
    port: 443,
    path: yandex_tts_url,
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`,
      'x-folder-id': folderId,
      'Content-Type': 'application/json',
      'Content-Length': Buffer.byteLength(body),
    },
  };

  const file = fs.createWriteStream(options['file']);
  const req = https.request(reqOptions, (res) => {
    let responseData = '';
    res.on('data', (chunk) => {
      responseData += chunk;
    });
    res.on('end', () => {
      if (res.statusCode !== 200) {
        console.error(`HTTP ${res.statusCode}: ${responseData}`);
        file.end();
        callback();
        return;
      }
      const lines = responseData.split('\n').filter((line) => line.trim());
      for (const line of lines) {
        try {
          const parsed = JSON.parse(line);
          if (parsed.result && parsed.result.audioChunk && parsed.result.audioChunk.data) {
            const audioBuffer = Buffer.from(parsed.result.audioChunk.data, 'base64');
            file.write(audioBuffer);
          }
        } catch (e) {
          // skip non-JSON lines
        }
      }
      file.end(callback);
    });
  });

  req.on('error', (e) => {
    console.error(`Request error: ${e.message}`);
    file.end();
    callback();
  });

  req.write(body);
  req.end();
}

const execSync = require('child_process').execSync;

function makeFile(text, filePath) {
  return new Promise((resolve) => {
    YandexTTS(
      {
        developer_key: token,
        text,
        file: filePath,
      },
      resolve,
    );
  });
}

let stack = new Set(words.split(','));

async function makeFiles() {
  for (let word of stack) {
    let fileName = `audio/${word.trim()}.ogg`;
    let wawFileName = `audio/${word.trim()}_.mp3`;
    let resultFileName = `audio/${word.trim()}.mp3`;
    await makeFile(word + '.', fileName);
    execSync(`ffmpeg -i ${fileName} ${wawFileName}`);
    execSync(
      `ffmpeg-normalize ${wawFileName} --normalization-type peak --target-level 0 -c:a libmp3lame -b:a 320k -o ${resultFileName}`,
    );
    fs.unlinkSync(fileName);
    fs.unlinkSync(wawFileName);
  }
}

makeFiles();
