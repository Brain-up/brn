package com.epam.brn.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.task.TaskExecutor
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor

@Configuration
class LastVisitUpdateConfig {
    @Bean("lastVisitUpdateExecutor")
    fun lastVisitUpdateExecutor(): TaskExecutor = ThreadPoolTaskExecutor().apply {
        setCorePoolSize(1)
        setMaxPoolSize(1)
        setQueueCapacity(1000)
        setThreadNamePrefix("last-visit-")
        initialize()
    }
}
