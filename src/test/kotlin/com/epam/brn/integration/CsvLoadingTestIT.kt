package com.epam.brn.integration

import com.epam.brn.repo.ExerciseGroupRepository
import com.epam.brn.repo.ExerciseRepository
import com.epam.brn.repo.ResourceRepository
import com.epam.brn.repo.SeriesRepository
import com.epam.brn.repo.TaskRepository
import com.epam.brn.service.InitialDataLoader
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
            uploadService: CsvUploadService
        ) = InitialDataLoader(
            resourceLoader,
            exerciseGroupRepository,
            uploadService
        )
    }

    @Autowired
    private lateinit var exerciseGroupRepository: ExerciseGroupRepository

    @Autowired
    private lateinit var seriesRepository: SeriesRepository

    @Autowired
    private lateinit var exerciseRepository: ExerciseRepository

    @Autowired
    private lateinit var taskRepository: TaskRepository

    @Autowired
    private lateinit var resourceRepository: ResourceRepository

    @Test
    fun `should load test data from classpath initFiles folder`() {
        resourceRepository.findAll() shouldHaveSize 167
        exerciseGroupRepository.findAll() shouldHaveSize 2
        seriesRepository.findAll() shouldHaveSize 5
        exerciseRepository.findAll() shouldHaveSize 13
        taskRepository.findAll() shouldHaveSize 69
    }
}
