## Why

When a learner opens a word/phrase task, the client asks the backend to speak the
text aloud: `GET /audio` streams a generated `.ogg` for the given text and exercise.
Behind that one endpoint sits a small pipeline — resolve the exercise, shape the
text, pick the voice, adapt the speed to how the user last performed — and a
pluggable text-to-speech provider (Yandex SpeechKit or Azure TTS) chosen by
configuration. None of this is written down, so the rules for "what audio comes
back and why" live only in the service code.

As a volunteer project without a BA, we want the request-time audio-generation
behaviour captured as an OpenSpec capability so the client/behaviour contract is
explicit and safe to change against.

This change is **documentation only** — it records existing behaviour as a new
`audio-generation` capability, in the same style as the specs already under
`openspec/specs/`. No production code changes.

## What Changes

- Add a new capability `audio-generation` describing:
  - **The audio endpoint** — `GET /audio`, available only when a TTS provider is
    configured, open to any authenticated user, streaming an `.ogg` byte stream.
  - **The request pipeline** — resolving the exercise (and not-found behaviour),
    shaping the text by exercise type, selecting the locale's default voice, and
    adapting speed to the user's last result for that exercise.
  - **The provider abstraction** — exactly one active TTS provider selected by
    configuration, request validation (supported locale / voice, default voice
    fallback), and surfacing a provider failure as an error.
- Back-fill characterization tests only for scenarios not already covered; no
  behaviour changes.

## Impact

- Affected specs: **new** `audio-generation`.
- Affected code: none (documentation); at most new characterization tests under
  `src/test/kotlin/com/epam/brn/`.
- Boundary: this capability owns the **request-time** audio-generation path only.
  Batch pre-generation of audio files by the startup data loaders is bootstrap code
  (excluded from coverage) and out of scope. The "done well / done badly" judgment
  used for speed adaptation is owned by the exercise-quality/availability
  capabilities and only referenced here. Audiometry, media upload, and voice
  catalogue administration are separate concerns.
