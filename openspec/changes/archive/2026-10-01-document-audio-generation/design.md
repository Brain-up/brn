## Context

See proposal.md — Why. This change documents the existing request-time
audio-generation behaviour; no code changes. The behaviour is realized by
`AudioController` (`GET /audio`, gated by `@ConditionalOnProperty(default.tts.provider)`
and `@RolesAllowed(USER)`), `UserAnalyticsServiceImpl.prepareAudioStreamForUser` /
`prepareAudioFileMetaData` / `setSpeedForUser` (exercise resolution, text shaping,
voice selection, speed adaptation), the `TextToSpeechService` strategy interface,
and its two implementations `YandexSpeechKitService` (`@Primary`, provider=yandex)
and `AzureTextToSpeechService` (provider=azure), with `WordsService` supplying the
locale's default voice and supported voices.

## Goals / Non-Goals

**Goals:**
- Capture the endpoint contract and the request pipeline (resolve exercise → shape
  text → select voice → adapt speed) as testable scenarios.
- Capture the single-active-provider abstraction, request validation, and
  failure-as-error behaviour without pinning provider wire formats.

**Non-Goals:**
- No new endpoints, parameters, or behaviour changes.
- No batch pre-generation of audio files by startup data loaders (bootstrap code,
  excluded from coverage) — only the request-time streaming path.
- No re-specification of the "done well / done badly" rule (owned by
  exercise-quality); it is referenced at the speed-adaptation boundary.
- No provider HTTP/SSML wire details, IAM-token mechanics, or voice-catalogue
  administration.

## Decisions

- **Provider-agnostic contract, provider-specific scenarios only where they differ.**
  The endpoint contract is stated once; provider selection is a configuration choice
  that must not change the contract. Validation is specified as observable behaviour
  (reject unsupported locale/voice, default-voice fallback) which matches the active
  validated path; exact per-provider fallback internals are left to implementation.
  - *Alternative considered*: write separate Yandex and Azure specs — rejected, it
    would duplicate the shared contract and leak implementation detail.

- **Voice is the locale default on this path.** The request-time pipeline currently
  overrides the requested voice with the locale's default woman voice (there is a
  code TODO to revisit after a SpeechKit upgrade). The spec documents the behaviour
  as it is today, not the eventual intent.

- **Speed adaptation is behaviour, not tuning.** No-history/bad/well → normal/slow/
  faster is observable and specified; the exact numeric speed values are left as
  implementation/tuning detail.

- **Document-only, verify by reading.** Confirm each scenario against the controller
  and services; back-fill a characterization test only where a scenario is not
  already covered. No production code is touched.

## Risks / Trade-offs

- [The written contract may not match runtime behaviour] → Ground every requirement
  in the actual controller/service code during apply; treat any mismatch as a bug
  report against the spec draft or the code, not a silent edit.
- [Over-specifying provider internals] → Specify only observable
  validation/failure behaviour; leave wire formats and token/caching mechanics out.
- [Scope creep into loaders/audiometry/media] → Explicit Non-Goals above.
