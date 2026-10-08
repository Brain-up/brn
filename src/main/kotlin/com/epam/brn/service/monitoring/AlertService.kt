package com.epam.brn.service.monitoring

/**
 * Transport-agnostic operational alerting. Implementations deliver a short
 * message to an ops channel (Telegram, Slack, …) so a broken integration is
 * noticed without reading logs or waiting for a user complaint.
 */
interface AlertService {
    /**
     * Deliver an operational alert. Implementations MUST be best-effort: a
     * failure to deliver the alert must never propagate to the caller, and
     * repeated identical alerts may be de-duplicated within a cooldown window.
     *
     * @param key stable identifier of the alert source (used for de-duplication), e.g. "yandex-tts"
     * @param message human-readable alert text
     */
    fun sendAlert(
        key: String,
        message: String,
    )
}
