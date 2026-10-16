package com.epam.brn.config

import org.apache.logging.log4j.kotlin.logger
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.AsyncConfigurer
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.util.concurrent.Executor

/**
 * Enables `@Async` execution for the precomputed-analytics listener with a bounded pool.
 * Loaded only when the precompute feature is enabled, so no executor exists while the feature is off.
 * The rejection handler logs dropped increments rather than failing or blocking the caller — the
 * nightly reconcile heals any resulting drift.
 */
@Configuration
@EnableAsync
@ConditionalOnProperty(name = ["brn.user.analytics.precompute.enabled"], havingValue = "true")
class AsyncConfig : AsyncConfigurer {
    private val log = logger()

    override fun getAsyncExecutor(): Executor {
        val executor = ThreadPoolTaskExecutor()
        executor.corePoolSize = 2
        executor.maxPoolSize = 4
        executor.queueCapacity = 500
        executor.setThreadNamePrefix("brn-analytics-")
        executor.setRejectedExecutionHandler { _, _ ->
            log.warn("Analytics increment rejected by async executor and dropped; nightly reconcile will heal the drift")
        }
        executor.initialize()
        return executor
    }
}
