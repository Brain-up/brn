## ADDED Requirements

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
