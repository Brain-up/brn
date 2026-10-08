# audio-generation Specification

## Purpose

Describe how the backend turns a piece of text into spoken audio at request time:
the `GET /audio` endpoint, the pipeline that resolves the exercise, shapes the text,
selects the voice and adapts the speed to the user, and the pluggable text-to-speech
provider that produces the audio. This capability owns the request-time generation
path only; batch pre-generation by startup loaders, audiometry, media upload, and
voice-catalogue administration belong elsewhere, and the "done well" judgment used
for speed adaptation is owned by the exercise-quality/availability capabilities.

## Requirements

### Requirement: Audio Endpoint Available Only When A TTS Provider Is Configured

The system SHALL expose the audio endpoint only when a text-to-speech provider is
configured. When no provider is configured the audio endpoint SHALL NOT be available.

#### Scenario: Provider configured

- **WHEN** a text-to-speech provider is configured
- **THEN** the audio endpoint is available

#### Scenario: No provider configured

- **WHEN** no text-to-speech provider is configured
- **THEN** the audio endpoint is not available

### Requirement: Stream Generated Speech For Text

The system SHALL, for any authenticated user, accept a request carrying the text to
speak and return the generated audio as an `.ogg` byte stream. The request MAY also
carry an exercise id, a locale (default `ru-ru`), and optional voice/speed/gender/
pitch/style hints. Text SHALL be required.

#### Scenario: Generate audio for text

- **WHEN** an authenticated user requests audio for a given text
- **THEN** the response is an octet-stream containing the generated `.ogg` audio

#### Scenario: Default locale

- **WHEN** a request does not specify a locale
- **THEN** the locale `ru-ru` is used

### Requirement: Resolve The Exercise For The Request

The system SHALL resolve the exercise referenced by the request to determine its
exercise type. When no exercise exists for the referenced id the system SHALL report
a not-found error and SHALL NOT generate audio.

#### Scenario: Exercise exists

- **WHEN** the request references an existing exercise
- **THEN** that exercise's type is used to shape the generation

#### Scenario: Exercise not found

- **WHEN** the request references an id that has no exercise
- **THEN** the system reports a not-found error and no audio is generated

### Requirement: Shape Text By Exercise Type

The system SHALL adjust the text before synthesis based on the resolved exercise
type. For word-like exercise types the system SHALL insert a separator between
space-delimited parts so the words are spoken as a list with pauses; for
sentence/phrase exercise types the text SHALL be spoken as written.

#### Scenario: Word-like exercise

- **WHEN** the exercise type is not a sentence or phrase type
- **THEN** spaces in the text are replaced with a comma-and-space separator before synthesis

#### Scenario: Sentence or phrase exercise

- **WHEN** the exercise type is a sentence or phrase type
- **THEN** the text is synthesized as written

### Requirement: Select The Locale's Default Voice

The system SHALL synthesize the audio using the default voice for the request's
locale.

#### Scenario: Voice chosen from locale

- **WHEN** audio is generated for a request
- **THEN** the default voice configured for the request's locale is used

### Requirement: Adapt Speed To The User's Last Result

The system SHALL set the playback speed from the requesting user's most recent study
history for the referenced exercise:

- no previous attempt → normal speed;
- the last attempt was done badly → slow speed;
- the last attempt was done well → faster speed.

The "done well / done badly" judgment is the one owned by the exercise-quality
capability.

#### Scenario: No previous attempt

- **WHEN** the user has no study history for the referenced exercise
- **THEN** the audio is generated at normal speed

#### Scenario: Last attempt was done badly

- **WHEN** the user's last attempt at the referenced exercise was done badly
- **THEN** the audio is generated at slow speed

#### Scenario: Last attempt was done well

- **WHEN** the user's last attempt at the referenced exercise was done well
- **THEN** the audio is generated at faster speed

### Requirement: Exactly One TTS Provider Selected By Configuration

The system SHALL use exactly one text-to-speech provider, selected by configuration,
to produce the audio. The supported providers are Yandex SpeechKit and Azure TTS.
Switching the provider SHALL NOT change the endpoint's contract.

#### Scenario: Yandex selected

- **WHEN** the configured provider is Yandex SpeechKit
- **THEN** audio is produced through the Yandex provider

#### Scenario: Azure selected

- **WHEN** the configured provider is Azure TTS
- **THEN** audio is produced through the Azure provider

### Requirement: Yandex SpeechKit Authenticates With A Service Account API Key

When the configured provider is Yandex SpeechKit, the system SHALL authenticate every
synthesis request to Yandex Cloud with a static service account API key, sent as an
`Authorization: Api-Key <key>` HTTP header. The API key SHALL be supplied by configuration.
The system SHALL NOT acquire or refresh an expiring IAM or OAuth token at runtime to
authenticate audio generation.

#### Scenario: Synthesis request carries the API key

- **WHEN** audio is produced through the Yandex SpeechKit provider
- **THEN** the synthesis request to Yandex Cloud carries an `Authorization: Api-Key` header
  built from the configured API key

#### Scenario: No runtime token exchange

- **WHEN** the Yandex SpeechKit provider produces audio
- **THEN** no separate token-acquisition request is made to Yandex Cloud

### Requirement: Validate The Request Before Synthesis

Before producing audio, the system SHALL validate the request against the active
provider's capabilities. An unsupported locale SHALL be rejected as an invalid
request. A voice that is not valid for the requested locale SHALL be rejected as an
invalid request. When no usable voice is specified the system SHALL fall back to the
locale's default voice rather than failing.

#### Scenario: Unsupported locale

- **WHEN** a request asks for a locale the provider does not support
- **THEN** the request is rejected as invalid and no audio is produced

#### Scenario: Unsupported voice for the locale

- **WHEN** a request specifies a voice that is not valid for the requested locale
- **THEN** the request is rejected as invalid and no audio is produced

#### Scenario: No voice specified

- **WHEN** a request specifies no usable voice
- **THEN** the locale's default voice is used

### Requirement: Surface A Provider Failure As An Error

When the active provider cannot produce the audio, the system SHALL raise an error
rather than returning empty or partial audio.

#### Scenario: Provider cannot produce audio

- **WHEN** the active text-to-speech provider fails to return audio
- **THEN** the system raises an error and does not return a partial or empty stream
