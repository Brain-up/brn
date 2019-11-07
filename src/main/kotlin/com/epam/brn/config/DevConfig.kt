package com.epam.brn.config

import com.epam.brn.model.Exercise
import com.epam.brn.model.ExerciseGroup
import com.epam.brn.model.Series
import com.epam.brn.model.StudyHistory
import com.epam.brn.model.UserAccount
import com.epam.brn.repo.ExerciseGroupRepository
import com.epam.brn.repo.ExerciseRepository
import com.epam.brn.repo.SeriesRepository
import com.epam.brn.repo.StudyHistoryRepository
import com.epam.brn.repo.UserAccountRepository
import org.apache.logging.log4j.kotlin.logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import java.time.LocalDate
import java.time.LocalDateTime

@Profile("dev")
@Configuration
class DevConfig(
    @Autowired val userAccountRepository: UserAccountRepository,
    @Autowired val exerciseRepository: ExerciseRepository,
    @Autowired val seriesRepository: SeriesRepository,
    @Autowired val studyHistoryRepository: StudyHistoryRepository,
    @Autowired val exerciseGroupRepository: ExerciseGroupRepository
) {
    private val log = logger()

    @Bean
    fun databaseInitializer() = ApplicationRunner {
        log.debug("------- Started DEV initialization")
        createDBMockContentValues()
        log.debug("------- Finished DEV initialization")
    }

    private fun createDBMockContentValues() {
        val exerciseGroup = exerciseGroupRepository.save(
            ExerciseGroup(
                id = 0,
                description = "desc",
                name = "group"
            )
        )
        log.debug("---- Created $exerciseGroup")

        val series = seriesRepository.save(
            Series(
                id = 0,
                description = "desc",
                name = "group",
                exerciseGroup = exerciseGroup
            )
        )
        log.debug("---- Created $series")

        val useraccount = userAccountRepository.save(
            UserAccount(
                id = 0,
                name = "manuel",
                birthDate = LocalDate.now(),
                email = "123@123.asd"
            )
        )
        log.debug("---- Created $useraccount")

        val exercise = exerciseRepository.save(
            Exercise(
                id = 0,
                description = toString(),
                series = series,
                level = 0,
                name = "exercise"
            )
        )
        log.debug("---- Created $exercise")

        val studyHistory = studyHistoryRepository.save(
            StudyHistory(
                id = 0,
                userAccount = useraccount,
                exercise = exercise,
                endTime = LocalDateTime.now(),
                startTime = LocalDateTime.now(),
                doneTasksCount = 2,
                successTasksCount = 1,
                repetitionCount = 3
            )
        )
        log.debug("---- Created $studyHistory")
    }
}