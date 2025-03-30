package com.epam.brn.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration

@Configuration
class UserDetailControllerConfig(
    @Value("\${brn.user.analytics.use.new.version}")
   val isUseNewAnalyticsService: Boolean
)
