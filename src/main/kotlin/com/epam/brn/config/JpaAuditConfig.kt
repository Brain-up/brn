package com.epam.brn.config

import com.epam.brn.service.TimeService
import com.epam.brn.service.UserAccountService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.auditing.DateTimeProvider
import org.springframework.data.domain.AuditorAware
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.security.core.context.SecurityContextHolder
import java.util.Optional

@Configuration
@EnableJpaAuditing(
    auditorAwareRef = "userContextProvider",
    dateTimeProviderRef = "dateTimeProvider"
)
class JpaAuditConfig {

    @Bean
    fun dateTimeProvider(dateTimeService: TimeService): DateTimeProvider {
        return DateTimeProvider {
            Optional.of(dateTimeService.now())
        }
    }

    @Bean
    fun userContextProvider(userAccountService: UserAccountService): AuditorAware<String> {
        return AuditorAware<String> {
            val authentication = SecurityContextHolder.getContext().authentication
            Optional.of(if (authentication != null) authentication.name else "Unauthorized")
        }
    }
}
