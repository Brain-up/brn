## 1. Validate the planning artifacts

- [x] 1.1 Run `openspec validate document-audio-generation --strict` and verify it passes with no errors

## 2. Confirm each requirement against the current code

- [x] 2.1 Verify "Audio Endpoint Available Only When A TTS Provider Is Configured" against `AudioController` `@ConditionalOnProperty(name = ["default.tts.provider"])` and `@RolesAllowed(USER)`
- [x] 2.2 Verify "Stream Generated Speech For Text" against `AudioController.getAudioByteArray` — required `text`, optional params, `ru-ru` default locale, octet-stream `.ogg` body via `prepareAudioStreamForUser`
- [x] 2.3 Verify "Resolve The Exercise For The Request" against `UserAnalyticsServiceImpl.prepareAudioFileMetaData` / `ExerciseRepository.findTypeByExerciseId` — type lookup and `EntityNotFoundException` on missing exercise
- [x] 2.4 Verify "Shape Text By Exercise Type" against `prepareAudioFileMetaData` — spaces → ", " for non-(SENTENCE/PHRASES) types, text unchanged for text types
- [x] 2.5 Verify "Select The Locale's Default Voice" against `prepareAudioFileMetaData` + `WordsService.getDefaultWomanVoiceForLocale`
- [x] 2.6 Verify "Adapt Speed To The User's Last Result" against `setSpeedForUser` / `StudyHistoryRepository.findLastByUserAccountIdAndExerciseId` and `AudioFileMetaData.setSpeedNormal/Slow/Faster` — null→normal, bad→slow, well→faster, using `ExerciseService.isDoneWell`
- [x] 2.7 Verify "Exactly One TTS Provider Selected By Configuration" against the `@ConditionalOnProperty(havingValue = "yandex"/"azure")` on `YandexSpeechKitService` (`@Primary`) and `AzureTextToSpeechService`, both implementing `TextToSpeechService`
- [x] 2.8 Verify "Validate The Request Before Synthesis" against `YandexSpeechKitService.validateLocaleAndVoice` (unsupported locale/voice → IllegalArgumentException) and empty-voice fallback to `getDefaultWomanVoiceForLocale`
- [x] 2.9 Verify "Surface A Provider Failure As An Error" against `YandexSpeechKitService` (non-200 → `YandexServiceException`) and `AzureTextToSpeechService` (null/empty → `AzureTtsException`)

## 3. Back-fill characterization tests for uncovered scenarios

- [x] 3.1 For any scenario in section 2 not already covered by an existing unit/integration test, add a characterization test in the standard stack (JUnit5 + MockK + kotest) and verify `gradlew verify` passes — no gap found: all nine requirements are already characterized by `AudioControllerTest`, `UserAnalyticsServiceTest` (text shaping, speed null/bad/well, default voice, exercise-not-found), `YandexSpeechKitServiceTest` (locale/voice validation, empty-voice fallback, non-200 → `YandexServiceException`), and `AzureTextToSpeechServiceTest`
- [x] 3.2 If any scenario does not match the code, stop and report the mismatch (spec draft vs. code) rather than editing code silently — record the resolution in the change before proceeding — no mismatch found

## 4. Finalize

- [x] 4.1 Run `gradlew verify` and confirm it exits 0 (ktlint + unit tests green)
- [x] 4.2 Re-run `openspec validate document-audio-generation --strict` and confirm the change is ready to archive
