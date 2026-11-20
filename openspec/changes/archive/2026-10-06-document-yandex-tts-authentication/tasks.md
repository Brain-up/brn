## 1. Verify the behaviour against the code

- [x] 1.1 Confirm `YandexSpeechKitService.generateAudioStream` sets
  `Authorization: Api-Key $apiKey` and performs no token-acquisition request.
- [x] 1.2 Confirm `yandex.apiKey` (env `YANDEX_API_KEY`) is the sole Yandex credential and
  that `yandex.authToken`/`yandex.getTokenLink` and the IAM-token flow are gone.
- [x] 1.3 Confirm the unit test
  `YandexSpeechKitServiceTest."should authenticate audio generation request with Api-Key header"`
  locks in the `Api-Key` auth header.

## 2. Record the requirement in the spec

- [x] 2.1 Add the "Yandex SpeechKit Authenticates With A Service Account API Key"
  requirement (with scenarios) to the `audio-generation` delta.
- [x] 2.2 Validate: `openspec validate document-yandex-tts-authentication --strict`.

## 3. Sync and archive

- [x] 3.1 Fold the requirement into `openspec/specs/audio-generation/spec.md`.
- [x] 3.2 Validate main specs: `openspec validate --specs`.
