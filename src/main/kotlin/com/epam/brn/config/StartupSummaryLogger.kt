package com.epam.brn.config

import org.apache.logging.log4j.kotlin.logger
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component

@Component
class StartupSummaryLogger(
    private val environment: Environment,
) {
    private val log = logger()

    @EventListener(ApplicationReadyEvent::class)
    fun logStartupSummary() {
        val isDev = environment.activeProfiles.contains("dev")
        val rows =
            listOf(
                "Active profile" to environment.activeProfiles.joinToString().ifBlank { "default" },
                "GitHub contributors sync" to onOff(!isDev),
                "Firebase user loader" to onOff(environment.getProperty("firebase.user.loader.enabled", Boolean::class.java, true)),
                "Firebase SSL verification" to onOff(!environment.getProperty("firebase.ssl.disable", Boolean::class.java, false)),
                "Cloud provider" to (environment.getProperty("cloud.provider") ?: "none"),
                "TTS provider" to (environment.getProperty("default.tts.provider") ?: "none"),
            )

        val label = "Startup configuration"
        val nameWidth = rows.maxOf { it.first.length }
        val texts = rows.map { (name, value) -> "${name.padEnd(nameWidth)} : $value" }
        val innerWidth = (texts.maxOf { it.length }).coerceAtLeast(label.length)
        val border = "─".repeat(innerWidth + 2)
        val box =
            buildString {
                append('\n')
                appendLine("┌$border┐")
                appendLine("│ ${label.padEnd(innerWidth)} │")
                appendLine("├$border┤")
                texts.forEach { appendLine("│ ${it.padEnd(innerWidth)} │") }
                append("└$border┘")
            }
        log.info(box)
    }

    private fun onOff(enabled: Boolean): String = if (enabled) "ON" else "OFF"
}
