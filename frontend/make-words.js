const fs = require('fs');
const request = require('request');
const qs = require('querystring');

const words = `бам,сам,дам,зал,бум`;
const token = '';
const folderId = '';

// pip install ffmpeg-normalize
// install ffmpeg

// yc iam create-token
// https://cloud.yandex.ru/docs/speechkit/tts/request

const yandex_tts_url =
  'https://tts.api.cloud.yandex.net/speech/v1/tts:synthesize?';

function YandexTTS(options, callback) {
  var params = {};

  params['text'] = options['text'];
  params['folderId'] = folderId;
  params['format'] = 'oggopus';
  params['lang'] = 'ru-RU';
  params['voice'] = 'filipp';
  params['emotion'] = 'good';

  var full_url = yandex_tts_url + qs.stringify(params);

  var file = fs.createWriteStream(options['file']);
  file.on('finish', callback);
  request({
    url: full_url,
    headers: {
      Authorization: `Bearer ${token}`,
    },
  }).pipe(file);
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
// stack.forEach((word)=>{
//     let file = word.trim();
//     execSync(`gtts-cli "${word}." -lang_check --lang ru --output ${file}.mp3`);
//     execSync(`ffmpeg-normalize ${file}.mp3 --normalization-type peak --target-level 0 -c:a libmp3lame -b:a 320k -o ${file}_n.mp3`)
// });
