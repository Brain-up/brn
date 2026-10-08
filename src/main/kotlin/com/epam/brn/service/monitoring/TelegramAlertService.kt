package com.epam.brn.service.monitoring

import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.ContentType
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.logging.log4j.kotlin.logger
import org.json.JSONObject
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap

/**
 * Delivers operational alerts to a Telegram chat via the Bot API.
 *
 * It is always registered as a bean but is a no-op unless explicitly enabled and
 * configured, so local/dev runs stay silent. Identical alerts for the same [key]
 * are suppressed within [minInterval] to avoid flooding the channel when an
 * integration fails on every request.
 */
@Service
class TelegramAlertService : AlertService {
    @Value("\${alert.telegram.enabled}")
    var enabled: Boolean = false

    @Value("\${alert.telegram.bot-token}")
    lateinit var botToken: String

    @Value("\${alert.telegram.chat-id}")
    lateinit var chatId: String

    @Value("\${alert.telegram.api-url}")
    lateinit var apiUrl: String

    @Value("\${alert.min-interval}")
    lateinit var minInterval: Duration

    private val lastSentByKey = ConcurrentHashMap<String, LocalDateTime>()

    private val log = logger()

    override fun sendAlert(
        key: String,
        message: String,
    ) {
        if (!enabled || botToken.isBlank() || chatId.isBlank()) {
            log.debug("Alerting disabled or not configured, skipping alert for key=$key")
            return
        }
        if (isOnCooldown(key)) {
            log.debug("Alert for key=$key is on cooldown, skipping")
            return
        }
        try {
            deliver(message)
            lastSentByKey[key] = LocalDateTime.now()
        } catch (e: Exception) {
            log.error("Failed to deliver alert for key=$key: ${e.message}", e)
        }
    }

    private fun isOnCooldown(key: String): Boolean {
        val lastSent = lastSentByKey[key] ?: return false
        return lastSent.plus(minInterval).isAfter(LocalDateTime.now())
    }

    private fun deliver(message: String) {
        val payload =
            JSONObject()
                .put("chat_id", chatId)
                .put("text", message)
                .toString()

        val postRequest = HttpPost("$apiUrl/bot$botToken/sendMessage")
        postRequest.entity = StringEntity(payload, ContentType.APPLICATION_JSON)

        val httpClient = HttpClientBuilder.create().build()
        httpClient.execute(postRequest).use { response ->
            val statusCode = response.statusLine.statusCode
            if (statusCode != HttpStatus.OK.value())
                log.error("Telegram alert was not accepted, httpStatus={$statusCode}")
        }
    }
}
