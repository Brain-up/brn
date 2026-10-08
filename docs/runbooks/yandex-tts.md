# Runbook: Yandex SpeechKit (TTS) authentication

Operational guide for when audio generation via Yandex stops working. This is an
operations runbook, not a behavioural spec — the behaviour itself is documented in
`openspec/specs/audio-generation/spec.md`.

## How auth works (current)

- The backend calls Yandex SpeechKit `tts:synthesize` with a **service account API key**,
  sent as header `Authorization: Api-Key <key>`.
- The key comes from config property `yandex.apiKey`, fed by the environment variable
  **`YANDEX_API_KEY`** (see `src/main/resources/application.properties`).
- An API key **does not expire** (it is valid until deleted). There is no runtime token
  exchange — the old Yandex Passport OAuth → IAM token flow was removed on 2026-10 because
  Yandex Cloud stopped accepting new OAuth tokens as of 2026-06-01.

Relevant code: `src/main/kotlin/com/epam/brn/service/YandexSpeechKitService.kt`.

## Proactive detection (so you don't learn it from a user)

Two mechanisms, both inside the backend:

1. **Alert on failure.** When Yandex returns a non-200, the backend sends a Telegram
   alert (`🔴 Yandex SpeechKit failed: httpStatus={...}`) in addition to logging and
   failing the request. Repeated alerts for the same source are suppressed within
   `alert.min-interval` (default 10 min) so the channel is not flooded.
2. **Health probe.** A scheduled job (`TtsHealthProbeJob`, cron `tts.probe.cron`,
   default once a day at 09:00) synthesizes a tiny phrase through the active provider. If
   the provider is down the synthesis fails and mechanism (1) fires — so an outage is
   caught even when no real user is generating audio (e.g. a key revoked overnight). The
   probe costs one synthesis call per run, so it is deliberately infrequent; lower the
   cron only if you need faster detection.

### Turning it on in a deployed environment

In the deployed stack the two on/off switches are hard-coded to `"true"` in
`docker-compose-run.yml` (`ALERT_TELEGRAM_ENABLED`, `TTS_PROBE_ENABLED`) — prod wants both
on. You only need to provide the two secrets, as **GitHub Actions secrets** (the deploy
workflows `sed` them into the compose file, exactly like `YANDEX_API_KEY`):

| GitHub secret | Purpose |
| --- | --- |
| `ALERT_TELEGRAM_BOT_TOKEN` | Telegram bot token (from @BotFather) |
| `ALERT_TELEGRAM_CHAT_ID` | target chat/channel id (see "Setting up the Telegram channel") |

After adding/changing a secret, redeploy so the value reaches the container
(`docker compose up -d --force-recreate brn`, or re-run the deploy workflow).

If the token secret is absent the backend still starts fine — a blank token makes the
alerter a silent no-op; it simply won't deliver alerts until the secret is set.

Locally everything defaults to **off** (`application.properties`: `alert.*`, `tts.probe.*`),
so dev runs and tests stay silent.

### Setting up the Telegram channel

1. In Telegram open **@BotFather** → `/newbot` → copy the **token** → this is
   `ALERT_TELEGRAM_BOT_TOKEN`.
2. Decide where alerts land and get the **chat id** → this is `ALERT_TELEGRAM_CHAT_ID`:
   - **DM**: open your bot, press **Start**, then message **@userinfobot** to get your
     numeric id.
   - **Group** (whole team sees it): create a group, add the bot, add **@getidsbot** to
     read the group id (a negative number).
   - **Channel**: make the bot an admin; the id is `@channelname` (public) or the numeric id.
3. Verify before deploying:
   ```bash
   curl -s "https://api.telegram.org/bot<TOKEN>/sendMessage" \
     -d chat_id=<CHAT_ID> -d text="тест алерта BrainUp"
   ```
   If the message arrives, put the same `<TOKEN>`/`<CHAT_ID>` into the GitHub secrets.

## Symptom

- Users get no generated audio.
- Backend logs show `YandexServiceException`, e.g.
  `Yandex cloud does not provide audio file for ..., httpStatus={401}` (or `403`/`400`).
- If alerting is enabled, a `🔴 Yandex SpeechKit failed` message appears in the ops channel.

## Diagnose

1. **Check the key the running container actually uses** (env is baked at container
   creation, not read live):
   ```bash
   docker exec brn printenv YANDEX_API_KEY
   ```
2. **Test the key directly** against Yandex (replace the key):
   ```bash
   curl -s -w '\nHTTP %{http_code}\n' -X POST \
     'https://tts.api.cloud.yandex.net/speech/v1/tts:synthesize' \
     -H 'Authorization: Api-Key <KEY>' \
     --data-urlencode 'text=проверка' \
     --data-urlencode 'lang=ru-RU' \
     --data-urlencode 'voice=oksana' \
     --data-urlencode 'format=oggopus' \
     --data-urlencode 'folderId=<FOLDER_ID>' --output /dev/null
   ```
   - `HTTP 200` → key is valid; the problem is that the container has a stale/wrong key
     (go to **Rotate / update**, step "update secret + recreate").
   - `HTTP 401/403` → key is invalid, deleted, or the service account lost its TTS role
     (go to **Rotate / update**, full flow).

## Rotate / update the key

1. In Yandex Cloud, ensure a **service account** exists in the project folder and has the
   role **`ai.speechKit-tts.user`** on the folder/cloud.
2. Create a new API key for it (console, or `yc iam api-key create --service-account-name <sa>`)
   and copy the `secret`.
3. Update the GitHub secret **`YANDEX_API_KEY`** with the new value.
4. Redeploy so the new value reaches the container. A plain restart is **not** enough —
   container environment is fixed at creation:
   ```bash
   docker compose up -d --force-recreate brn
   ```
   (via CI: re-run the deploy workflow, which `sed`s `_YANDEX_API_KEY_` into
   `docker-compose.yml` and recreates the container.)
5. Verify: trigger an audio request and confirm `200`; the log line
   `Ogg audio file ... was successfully generated by yandex!` appears.

## Gotchas

- `docker restart` / `docker compose restart` do **not** pick up a changed `YANDEX_API_KEY`
  — the env is set when the container is created. Always `up -d --force-recreate`.
- API keys do not expire, so unlike the old OAuth token this is a one-time setup — a sudden
  `401` usually means the key was deleted/rotated in Yandex or the service account lost its role.
- The old `YANDEX_AUTH_TOKEN` secret and `yandex.authToken`/`yandex.getTokenLink` properties
  are gone; if you see them anywhere, that config is stale.
