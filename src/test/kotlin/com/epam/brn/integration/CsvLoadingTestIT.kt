package com.epam.brn.integration

import com.epam.brn.repo.AudiometryRepository
import com.epam.brn.repo.AudiometryTaskRepository
import com.epam.brn.repo.ExerciseGroupRepository
import com.epam.brn.repo.ExerciseRepository
import com.epam.brn.repo.ResourceRepository
import com.epam.brn.repo.RoleRepository
import com.epam.brn.repo.SeriesRepository
import com.epam.brn.repo.SubGroupRepository
import com.epam.brn.repo.TaskRepository
import com.epam.brn.repo.UserAccountRepository
import com.epam.brn.service.RoleService
import com.epam.brn.service.load.AudiometryLoader
import com.epam.brn.service.load.InitialDataLoader
import com.epam.brn.upload.CsvUploadService
import io.kotest.matchers.collections.shouldHaveSize
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.core.io.ResourceLoader
import org.springframework.security.crypto.password.PasswordEncoder

class CsvLoadingTestIT : BaseIT() {
    @TestConfiguration
    class Config {
        @Bean
        fun initialDataLoader(
            resourceLoader: ResourceLoader,
            userAccountRepository: UserAccountRepository,
            audiometryLoader: AudiometryLoader,
            passwordEncoder: PasswordEncoder,
            roleService: RoleService,
            uploadService: CsvUploadService,
        ) = InitialDataLoader(
            resourceLoader,
            userAccountRepository,
            audiometryLoader,
            passwordEncoder,
            roleService,
            uploadService,
        )
    }

    @Autowired
    private lateinit var exerciseGroupRepository: ExerciseGroupRepository

    @Autowired
    private lateinit var userAccountRepository: UserAccountRepository

    @Autowired
    private lateinit var seriesRepository: SeriesRepository

    @Autowired
    private lateinit var subGroupRepository: SubGroupRepository

    @Autowired
    private lateinit var exerciseRepository: ExerciseRepository

    @Autowired
    private lateinit var taskRepository: TaskRepository

    @Autowired
    private lateinit var resourceRepository: ResourceRepository

    @Autowired
    private lateinit var roleRepository: RoleRepository

    @Autowired
    private lateinit var audiometryRepository: AudiometryRepository

    @Autowired
    private lateinit var audiometryTaskRepository: AudiometryTaskRepository

    @Test
    fun `should load test data from classpath initFiles folder`() {
        audiometryRepository.findAll() shouldHaveSize 6
        audiometryTaskRepository.findAll() shouldHaveSize 24
        exerciseGroupRepository.findAll() shouldHaveSize 4
        seriesRepository.findAll() shouldHaveSize 17
        subGroupRepository.findAll() shouldHaveSize 147
//        exerciseRepository.findAll() shouldHaveSize 188
//        taskRepository.findAll() shouldHaveSize 188
//        resourceRepository.findAll() shouldHaveSize 881
        userAccountRepository.findAll() shouldHaveSize 5
        roleRepository.findAll() shouldHaveSize 3
    }

    @AfterEach
    fun deleteAfterTest() {
        exerciseRepository.deleteAll()
        subGroupRepository.deleteAll()
        seriesRepository.deleteAll()
        exerciseGroupRepository.deleteAll()
        userAccountRepository.deleteAll()
        audiometryTaskRepository.deleteAll()
        audiometryRepository.deleteAll()
    }
}
