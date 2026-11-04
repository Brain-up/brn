package com.epam.brn.config

import org.apache.logging.log4j.kotlin.logger
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.AsyncConfigurer
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.util.concurrent.Executor

/**
 * Bounded `@Async` pool for analytics increments (only when precompute is enabled).
 * Rejected tasks are logged and dropped; the nightly reconcile heals the drift.
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
