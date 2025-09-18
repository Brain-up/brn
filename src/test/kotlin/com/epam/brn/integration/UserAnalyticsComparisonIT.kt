package com.epam.brn.integration

import com.epam.brn.config.AwsConfig
import com.epam.brn.job.UserAnalyticsJob
import com.epam.brn.model.UserAnalytics
import com.epam.brn.repo.UserAnalyticsRepository
import com.epam.brn.service.ExerciseService
import com.epam.brn.service.TimeService
import com.epam.brn.service.UrlConversionService
import com.epam.brn.service.WordsService
import com.epam.brn.service.YandexSpeechKitService
import com.epam.brn.service.cloud.AwsCloudService
import com.epam.brn.service.impl.HeadphonesServiceImpl
import com.epam.brn.service.impl.RoleServiceImpl
import com.epam.brn.service.impl.UserAccountServiceImpl
import com.epam.brn.service.impl.UserAnalyticsServiceImpl
import com.epam.brn.service.impl.UserAnalyticsServiceV1Impl
import com.epam.brn.service.statistics.impl.UserDayStatisticsService
import com.epam.brn.service.statistics.progress.status.impl.StudyHistoriesProgressStatusManager
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.data.domain.Pageable
import org.springframework.test.context.jdbc.Sql
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@DataJpaTest
@Tag("integration-test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Import(
    *[
        UserAnalyticsJob::class, UserAnalyticsServiceImpl::class, UserAnalyticsServiceV1Impl::class,
        UserDayStatisticsService::class, UserAccountServiceImpl::class, RoleServiceImpl::class, HeadphonesServiceImpl::class,
        TimeService::class, StudyHistoriesProgressStatusManager::class, YandexSpeechKitService::class, YandexSpeechKitService::class,
        WordsService::class, ExerciseService::class, UrlConversionService::class, AwsCloudService::class,
        AwsConfig::class,
    ],
)
class UserAnalyticsComparisonIT {
    @Autowired
    lateinit var userAnalyticsJob: UserAnalyticsJob

    @Autowired
    lateinit var userAnalyticsRepository: UserAnalyticsRepository

    @Autowired
    lateinit var userAnalyticsServiceImpl: UserAnalyticsServiceImpl

    @Autowired
    lateinit var userAnalyticsServiceV1Impl: UserAnalyticsServiceV1Impl

    @Test
    @Sql("/db/init-study-history.sql")
    fun `test filling user analytics job`() {
        // GIVEN
        val now = LocalDateTime.now()
        val firstDone = now.minusHours(1L).truncatedTo(ChronoUnit.SECONDS)
        val lastDone = now.plusHours(1L).truncatedTo(ChronoUnit.SECONDS)
        val roleUser = "USER"
        val roleAdmin = "ADMIN"

        val userAnalytics1 = UserAnalytics(1, 1, firstDone, lastDone, 50, 2, 1, roleAdmin)
        val userAnalytics2 = UserAnalytics(2, 1, firstDone, lastDone, 50, 2, 1, roleUser)
        val userAnalytics3 = UserAnalytics(3, 2, firstDone, lastDone, 36, 2, 1, roleUser)

        // WHEN
        userAnalyticsJob.fillUserAnalytics()
        val usersWithAnalytics = userAnalyticsServiceImpl.getUsersWithAnalytics(Pageable.unpaged(), "USER")
        val usersWithAnalytics1 = userAnalyticsServiceV1Impl.getUsersWithAnalytics(Pageable.unpaged(), "USER")
        val userAnalyticsList = userAnalyticsRepository.findAll()

        // THEN
        userAnalyticsList.size shouldBe 3
        userAnalyticsList shouldContainAll listOf(userAnalytics1, userAnalytics2, userAnalytics3)
        usersWithAnalytics.size shouldBe usersWithAnalytics1.size
        usersWithAnalytics shouldContainAll usersWithAnalytics1
    }
}
