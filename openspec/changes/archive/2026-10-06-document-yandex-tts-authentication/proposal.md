## Why

Yandex Cloud stopped accepting new Yandex Passport OAuth tokens on 2026-06-01, so the
backend's IAM-token acquisition for Yandex SpeechKit started failing with HTTP 400 and
audio generation broke. The fix migrated Yandex authentication to a static service
account **API key** (`Authorization: Api-Key`), removing the whole runtime token-exchange
flow. The `audio-generation` spec documents provider selection but says nothing about how
the Yandex provider authenticates, so code and spec are now out of sync. This change
records the real, shipped authentication behaviour.

## What Changes

- Document that when Yandex SpeechKit is the active provider, each synthesis request is
  authenticated with a configured service account API key sent as an
  `Authorization: Api-Key <key>` header, and that no expiring IAM/OAuth token is obtained
  or refreshed at runtime.
- No endpoint, request/response, role, or database-schema changes. This is a
  documentation change describing behaviour already implemented and tested.

## Capabilities

### New Capabilities

(none)

### Modified Capabilities

- `audio-generation`: add a requirement describing Yandex SpeechKit authentication via a
  static service account API key, replacing the previously undocumented (and now removed)
  runtime IAM/OAuth token exchange.

## Impact

- `openspec/specs/audio-generation/spec.md` — add the Yandex authentication requirement.
- Behaviour source: `src/main/kotlin/com/epam/brn/service/YandexSpeechKitService.kt`
  (sends `Authorization: Api-Key $apiKey`, no token acquisition) and
  `yandex.apiKey` in `src/main/resources/application.properties`.
- Operational guidance (rotation, diagnosis) lives in `docs/runbooks/yandex-tts.md`, not
  in the spec.
