package com.epam.brn.integration

import com.epam.brn.auth.AuthorityService
import com.epam.brn.repo.AuthorityRepository
import com.epam.brn.repo.ExerciseGroupRepository
import com.epam.brn.repo.ExerciseRepository
import com.epam.brn.repo.ResourceRepository
import com.epam.brn.repo.SeriesRepository
import com.epam.brn.repo.TaskRepository
import com.epam.brn.repo.UserAccountRepository
import com.epam.brn.service.AudioFilesGenerationService
import com.epam.brn.service.InitialDataLoader
import com.epam.brn.service.WordsService
import com.epam.brn.upload.CsvUploadService
import org.amshove.kluent.shouldHaveSize
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.core.io.ResourceLoader
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("integration-tests")
@Tag("integration-test")
class CsvLoadingTestIT {

    @TestConfiguration
    class Config {
        @Bean
        fun handBookLoader(
            resourceLoader: ResourceLoader,
            exerciseGroupRepository: ExerciseGroupRepository,
            userAccountRepository: UserAccountRepository,
            passwordEncoder: PasswordEncoder,
            authorityService: AuthorityService,
            uploadService: CsvUploadService,
            audioFilesGenerationService: AudioFilesGenerationService,
            wordsService: WordsService
        ) = InitialDataLoader(
            resourceLoader,
            exerciseGroupRepository,
            userAccountRepository,
            passwordEncoder,
            authorityService,
            uploadService,
            audioFilesGenerationService,
            wordsService
        )
    }

    @Autowired
    private lateinit var exerciseGroupRepository: ExerciseGroupRepository

    @Autowired
    private lateinit var userAccountRepository: UserAccountRepository

    @Autowired
    private lateinit var seriesRepository: SeriesRepository

    @Autowired
    private lateinit var exerciseRepository: ExerciseRepository

    @Autowired
    private lateinit var taskRepository: TaskRepository

    @Autowired
    private lateinit var resourceRepository: ResourceRepository

    @Autowired
    private lateinit var authorityRepository: AuthorityRepository

    @Test
    fun `should load test data from classpath initFiles folder`() {
        exerciseGroupRepository.findAll() shouldHaveSize 2
        seriesRepository.findAll() shouldHaveSize 4
        exerciseRepository.findAll() shouldHaveSize 99
        taskRepository.findAll() shouldHaveSize 107
        resourceRepository.findAll() shouldHaveSize 400
        userAccountRepository.findAll() shouldHaveSize 3
        authorityRepository.findAll() shouldHaveSize 2
    }
}
